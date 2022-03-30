package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.ConditionalNodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape

class ConditionalShapeTransformer(shape: AnyShape, override val ctx: ShapeTransformationContext)(
    implicit eh: AMFErrorHandler)
    extends ExtendedSchemaTransformer(shape, ctx)
    with ShapeTransformer {

  val mapping: ConditionalNodeMapping = ConditionalNodeMapping(shape.annotations).withId(shape.id)

  def transform(): ConditionalNodeMapping = {

    setMappingName(shape, mapping)
    setMappingId(mapping)
    updateContext(mapping)

    Option(shape.ifShape).foreach {
      case ifShape: AnyShape =>
        val transformed = ShapeTransformation(ifShape, ctx).transform()
        mapping.withIfMapping(transformed.id)
    }

    val transformedThen = Option(shape.thenShape) match {
      case Some(thenShape) if thenShape.isInstanceOf[AnyShape] =>
        val transformed = ShapeTransformation(thenShape.asInstanceOf[AnyShape], ctx).transform()
        addExtendedSchema(transformed)
        transformed
      case None =>
        extendedSchema.getOrElse(
          ShapeTransformation(TransformationHelper.dummyShape(mapping.id + "/then"), ctx).transform())
    }
    mapping.withThenMapping(transformedThen.id)

    val transformedElse = Option(shape.elseShape) match {
      case Some(elseShape) if elseShape.isInstanceOf[AnyShape] =>
        val transformed = ShapeTransformation(elseShape.asInstanceOf[AnyShape], ctx).transform()
        addExtendedSchema(transformed)
        transformed
      case None =>
        extendedSchema.getOrElse(
          ShapeTransformation(TransformationHelper.dummyShape(mapping.id + "/else"), ctx).transform())
    }
    mapping.withElseMapping(transformedElse.id)

    mapping
  }
}

object ConditionalShapeTransformer {
  def apply(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler) =
    new ConditionalShapeTransformer(shape, ctx)
}
