package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{AnyMapping, NodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.AnyShape

class AnyShapeTransformer(shape: AnyShape, override val ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends ShapeTransformer {

  // This should be a AnyMapping and not a NodeMapping
  val mapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def transform(): AnyMapping = {
    setMappingName(shape, mapping)
    setMappingId(mapping)
    updateContext(mapping)

    if (shape.and.nonEmpty) transformAnd()
    if (shape.xone.nonEmpty) transformXOne()

    mapping
  }

  private def transformAnd(): Unit  = mapping.withAnd(collectMembers(shape.and))
  private def transformXOne(): Unit = mapping.withOr(collectMembers(shape.xone))

  private def collectMembers(elements: Seq[Shape]): Seq[String] = elements.map {
    case member: AnyShape =>
      val transformed = ShapeTransformation(member, ctx).transform()
      transformed.id
  }

}

object AnyShapeTransformer {
  def apply(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler) =
    new AnyShapeTransformer(shape, ctx)
}
