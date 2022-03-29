package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.ConditionalNodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape

case class ConditionalShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends ExtendedSchemaTransformer(shape, ctx)
    with ShapeTransformer {

  val conditionalMapping: ConditionalNodeMapping = ConditionalNodeMapping(shape.annotations).withId(shape.id)

  def transform(): ConditionalNodeMapping = {

    setMappingName(shape, conditionalMapping)
    setMappingId(conditionalMapping)
    updateContext(conditionalMapping)

    Option(shape.ifShape).foreach {
      case ifShape: AnyShape =>
        val transformed = ShapeTransformation(ifShape, ctx).transform()
        conditionalMapping.withIfMapping(transformed.id)
    }

    val transformedThen = Option(shape.thenShape) match {
      case Some(thenShape) if thenShape.isInstanceOf[AnyShape] =>
        val transformed = ShapeTransformation(thenShape.asInstanceOf[AnyShape], ctx).transform()
        addExtendedSchema(transformed)
        transformed
      case None =>
        extendedSchema.getOrElse(
          ShapeTransformation(TransformationHelper.dummyShape(conditionalMapping.id + "/then"), ctx).transform())
    }
    conditionalMapping.withThenMapping(transformedThen.id)

    val transformedElse = Option(shape.elseShape) match {
      case Some(elseShape) if elseShape.isInstanceOf[AnyShape] =>
        val transformed = ShapeTransformation(elseShape.asInstanceOf[AnyShape], ctx).transform()
        addExtendedSchema(transformed)
        transformed
      case None =>
        extendedSchema.getOrElse(
          ShapeTransformation(TransformationHelper.dummyShape(conditionalMapping.id + "/else"), ctx).transform())
    }
    conditionalMapping.withElseMapping(transformedElse.id)

    conditionalMapping
  }
}
