package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{ConditionalNodeMapping, NodeMapping}
import amf.aml.internal.metamodel.domain.ConditionalNodeMappingModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel

case class ConditionalShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends ExtendedSchemaTransformer(shape, ctx)
    with ShapeTransformer {

  val conditionalMapping: ConditionalNodeMapping = ConditionalNodeMapping(shape.annotations).withId(shape.id)

  def transform(): ConditionalNodeMapping = {

    setMappingName(shape, conditionalMapping)
    setMappingId(conditionalMapping)
    updateContext(conditionalMapping)

    processConditionalComponent(AnyShapeModel.If, ConditionalNodeMappingModel.If)
    processConditionalComponent(AnyShapeModel.Then, ConditionalNodeMappingModel.Then)
    processConditionalComponent(AnyShapeModel.Else, ConditionalNodeMappingModel.Else)

    conditionalMapping
  }

  private def processConditionalComponent(fieldShape: Field, fieldDialect: Field): Unit =
    Option(shape.fields.get(fieldShape)).foreach {
      case component: AnyShape =>
        val transformed = ShapeTransformation(component, ctx).transform()
        if (fieldShape != AnyShapeModel.If) addExtendedSchema(transformed)
        conditionalMapping.set(fieldDialect, getIri(transformed).head)
    }
}
