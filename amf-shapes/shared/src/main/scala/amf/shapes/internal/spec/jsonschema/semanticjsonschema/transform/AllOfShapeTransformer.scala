package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape

case class AllOfShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends ExtendedSchemaTransformer(shape, ctx)
    with ShapeTransformer {

  val nodeMapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def transform(): NodeMapping = {

    setMappingName(shape, nodeMapping)
    setMappingId(nodeMapping)
    updateContext(nodeMapping)

    val members = shape.and.map {
      case member: AnyShape =>
        val transformed = ShapeTransformation(member, ctx).transform()
        toLink(transformed)
    }

    addExtendedSchema(nodeMapping, members)

    nodeMapping
  }
}
