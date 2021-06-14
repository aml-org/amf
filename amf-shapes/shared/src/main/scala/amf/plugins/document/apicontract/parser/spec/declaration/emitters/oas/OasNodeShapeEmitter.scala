package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.common.{
  Draft2019DependenciesEmitter,
  Draft4DependenciesEmitter,
  TypeEmitterFactory
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.emitter.UnevaluatedEmitter.unevaluatedPropertiesInfo
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.emitter.{
  UnevaluatedEmitter,
  UntranslatableDraft2019FieldsPresentGuard
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  OasTypeFacetEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  OAS30SchemaVersion
}
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.metamodel.NodeShapeModel.{
  AdditionalPropertiesSchema,
  UnevaluatedProperties,
  UnevaluatedPropertiesSchema
}
import amf.plugins.domain.shapes.models.NodeShape

import scala.collection.mutable.ListBuffer

case class OasNodeShapeEmitter(node: NodeShape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               schemaPath: Seq[(String, String)] = Nil,
                               isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext)
    extends OasAnyShapeEmitter(node, ordering, references, isHeader = isHeader) {
  override def emitters(): Seq[EntryEmitter] = {
    val isOas3 = spec.schemaVersion.isInstanceOf[OAS30SchemaVersion]

    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = node.fields

    result += OasTypeFacetEmitter("object", node)

    fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

    fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

    val additionalPropertiesSchema = fs.entry(AdditionalPropertiesSchema)

    additionalPropertiesSchema match {
      case Some(f) =>
        result += OasEntryShapeEmitter("additionalProperties",
                                       f.element.asInstanceOf[Shape],
                                       ordering,
                                       references,
                                       pointer,
                                       schemaPath)
      case None =>
        fs.entry(NodeShapeModel.Closed)
          .filter(f => isExplicit(f) || f.scalar.toBool)
          .foreach(f => result += ValueEmitter("additionalProperties", f.negated))
    }

    UntranslatableDraft2019FieldsPresentGuard(node,
                                              Seq(UnevaluatedPropertiesSchema, UnevaluatedProperties),
                                              Seq("unevaluatedProperties")).evaluateOrRun { () =>
      result += new UnevaluatedEmitter(node, unevaluatedPropertiesInfo, ordering, references, pointer, schemaPath)
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

    val emitterFactory: TypeEmitterFactory = shape =>
      OasTypeEmitter(shape, ordering, Seq(), references, pointer, schemaPath, isHeader)

    if (spec.schemaVersion == JSONSchemaDraft201909SchemaVersion) {
      result += Draft2019DependenciesEmitter(node, ordering, typeFactory = emitterFactory)
    } else {
      result += Draft4DependenciesEmitter(node, ordering, isRamlExtension = false, typeFactory = emitterFactory)
    }

    fs.entry(NodeShapeModel.Inherits).map(f => result += OasShapeInheritsEmitter(f, ordering, references))

    if (spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft7SchemaVersion) && Option(node.propertyNames).isDefined)
      result += OasEntryShapeEmitter("propertyNames", node.propertyNames, ordering, references, pointer, schemaPath)

    result
  }

  private def isExplicit(f: FieldEntry) = f.value.annotations.contains(classOf[ExplicitField])
}
