package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.Emitter
import amf.plugins.document.apicontract.annotations.{ExternalReferenceUrl, ForceEntry, ParsedJSONSchema}
import amf.plugins.document.apicontract.parser.spec.declaration.RamlLocalReferenceEntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.common.RamlExternalReferenceUrlEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  CommentEmitter,
  RamlShapeEmitterContext,
  SimpleTypeEmitter
}
import amf.plugins.domain.shapes.models.{FileShape, ScalarShape, _}
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import org.yaml.model.YType

case class Raml08TypeEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: RamlShapeEmitterContext) {

  def emitters(): Seq[Emitter] = {
    shape match {
      case shape: Shape if shape.isLink                => Seq(emitLink(shape))
      case s: Shape if inheritsFromParsedJsonSchema(s) => Seq(Raml08InheritedJsonSchemaEmitter(shape, ordering))
      case _ if Option(shape).isDefined && wasParsedFromExternalReference =>
        Seq(RamlExternalReferenceUrlEmitter(shape)())
      case shape: AnyShape if isParsedJsonSchema(shape) =>
        Seq(RamlJsonShapeEmitter(shape, ordering, Nil, typeKey = "schema"))
      case scalar: ScalarShape  => SimpleTypeEmitter(scalar, ordering).emitters()
      case array: ArrayShape    => emitArray(array)
      case union: UnionShape    => Seq(Raml08UnionEmitter(union, ordering))
      case schema: SchemaShape  => Seq(RamlSchemaShapeEmitter(schema, ordering, Nil))
      case nil: NilShape        => RamlNilShapeEmitter(nil, ordering, Seq()).emitters()
      case fileShape: FileShape => Seq(Raml08FileShapeEmitter(fileShape, ordering))
      case shape: AnyShape      => RamlAnyShapeEmitter(shape, ordering, Nil).emitters()
      case other =>
        Seq(CommentEmitter(other, s"Unsupported shape class for emit raml 08 spec ${other.getClass.toString}`"))
    }
  }

  private def emitArray(array: ArrayShape) = {
    array.items match {
      case sc: ScalarShape =>
        SimpleTypeEmitter(sc, ordering).emitters() :+ MapEntryEmitter("repeat", "true", YType.Bool)
      case f: FileShape =>
        val scalar =
          ScalarShape(f.fields, f.annotations)
            .withDataType(XsdTypeDefMapping.xsdFromString("file")._1.get)
        SimpleTypeEmitter(scalar, ordering).emitters() :+ MapEntryEmitter("repeat", "true", YType.Bool)
      case other =>
        Seq(CommentEmitter(other, s"Cannot emit array shape with items ${other.getClass.toString} in raml 08"))
    }
  }

  private def isParsedJsonSchema(shape: Shape)       = shape.annotations.contains(classOf[ParsedJSONSchema])
  private def wasParsedFromExternalReference         = shape.annotations.contains(classOf[ExternalReferenceUrl])
  private def shouldForceEntry(shape: Shape)         = shape.annotations.contains(classOf[ForceEntry])
  private def inheritsFromParsedJsonSchema(s: Shape) = s.inherits.exists(isParsedJsonSchema)

  private def emitLink(shape: Shape) = {
    if (shouldForceEntry(shape)) new RamlLocalReferenceEntryEmitter("type", shape)
    else spec.localReference(shape)
  }
}
