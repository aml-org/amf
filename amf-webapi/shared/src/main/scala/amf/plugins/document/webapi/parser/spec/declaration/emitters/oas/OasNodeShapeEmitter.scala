package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.annotations.ExplicitField
import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OAS30SchemaVersion}
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.models.NodeShape
import amf.core.utils.AmfStrings

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

case class OasNodeShapeEmitter(node: NodeShape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil,
                               isHeader: Boolean = false)(implicit spec: OasLikeSpecEmitterContext)
    extends OasAnyShapeEmitter(node, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val isOas3 = spec.schemaVersion.isInstanceOf[OAS30SchemaVersion]

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = node.fields

    result += spec.oasTypePropertyEmitter("object", node)

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    fs.entry(NodeShapeModel.Closed)
      .filter(f => f.value.annotations.contains(classOf[ExplicitField]) || f.scalar.toBool) match {
      case Some(f) => result += ValueEmitter("additionalProperties", f.negated)
      case _ =>
        fs.entry(NodeShapeModel.AdditionalPropertiesSchema)
          .map(
            f =>
              result += OasEntryShapeEmitter("additionalProperties",
                                             f.element.asInstanceOf[Shape],
                                             ordering,
                                             references,
                                             pointer,
                                             schemaPath))
    }

    if (isOas3) {
      fs.entry(NodeShapeModel.Discriminator)
        .orElse(fs.entry(NodeShapeModel.DiscriminatorMapping))
        .map(f => result += Oas3DiscriminatorEmitter(f, fs, ordering))
    } else {
      fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))
    }

    fs.entry(NodeShapeModel.DiscriminatorValue)
      .map(f => result += ValueEmitter("discriminatorValue".asOasExtension, f))

    fs.entry(NodeShapeModel.Properties).map(f => result += OasRequiredPropertiesShapeEmitter(f, references))

    fs.entry(NodeShapeModel.Properties)
      .map(f =>
        result += OasPropertiesShapeEmitter(f, ordering, references, pointer = pointer, schemaPath = schemaPath))

    val properties = ListMap(node.properties.map(p => p.id -> p): _*)

    fs.entry(NodeShapeModel.Dependencies).map(f => result += OasShapeDependenciesEmitter(f, ordering, properties))

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    if (spec.schemaVersion == JSONSchemaDraft7SchemaVersion && Option(node.propertyNames).isDefined)
      result += OasEntryShapeEmitter("propertyNames", node.propertyNames, ordering, references, pointer, schemaPath)

    result
  }
}
