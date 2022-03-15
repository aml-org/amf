package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.UnionNodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.AnyShape

case class OneOfShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends ExtendedSchemaTransformer(shape, ctx)
    with ShapeTransformer {

  val nodeMapping: UnionNodeMapping = UnionNodeMapping(shape.annotations).withId(shape.id)

  def transform(): UnionNodeMapping = {

    setMappingName(shape, nodeMapping)
    setMappingId(nodeMapping)
    updateContext(nodeMapping)

    val members = shape.xone.flatMap {
      case member: AnyShape =>
        val transformed = ShapeTransformation(member, ctx).transform()
        addExtendedSchema(transformed)
        getIri(transformed)
    }

    nodeMapping.withObjectRange(members)
  }
}
