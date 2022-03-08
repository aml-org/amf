package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{NodeMapping, UnionNodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.Fields
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class NodeShapeTransformer(shape: NodeShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler)
    extends ShapeTransformer {

  val nodeMapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def transform(): NodeMapping = {
    setMappingName(shape, nodeMapping)
    setMappingId(nodeMapping)
    updateContext(nodeMapping)

    val propertyMappings = shape.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }
    nodeMapping.withPropertiesMapping(propertyMappings)

    checkInheritance()
    checkSemantics()
    nodeMapping
  }

  private def checkInheritance(): Unit = {
    val superSchemas = shape.and
    if (superSchemas.nonEmpty) { // @TODO: support more than 1 super schema
      val hierarchy = superSchemas.map {
        case s: AnyShape =>
          val transformed = ShapeTransformation(s, ctx).transform()
          transformed match {
            case nm: NodeMapping       => nm.link[NodeMapping](nm.name.value())
            case unm: UnionNodeMapping => unm.link[UnionNodeMapping](unm.name.value())
          }
      }
      nodeMapping.withExtends(hierarchy)
    }
  }

  private def checkSemantics(): Unit = {
    // @TODO: support multiple type mappings
    ctx.semantics.typeMappings.headOption
      .flatMap(_.option())
      .foreach { typeMapping =>
        nodeMapping.withNodeTypeMapping(typeMapping)
      }
  }
}
