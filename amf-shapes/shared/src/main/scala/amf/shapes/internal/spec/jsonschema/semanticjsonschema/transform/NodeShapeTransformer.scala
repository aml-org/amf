package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.NodeShape

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
    nodeMapping.withClosed(false)

    checkAdditionalProperties()
    checkSemantics()
    nodeMapping
  }

  private def checkAdditionalProperties() = {
    shape.closed.option() match {
      case Some(value) => nodeMapping.withClosed(value)
      case None        => nodeMapping.withClosed(false)
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
