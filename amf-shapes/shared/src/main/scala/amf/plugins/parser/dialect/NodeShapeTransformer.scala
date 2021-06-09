package amf.plugins.parser.dialect

import amf.core.parser.Fields
import amf.plugins.document.vocabularies.model.domain.NodeMapping
import amf.plugins.domain.shapes.models.NodeShape

case class NodeShapeTransformer(node: NodeShape, ctx: ShapeTransformationContext) {

  val nodeMapping = NodeMapping(Fields(),node.annotations).withId(node.id)

  def transform(): NodeMapping = {
    updateContext()
    val propertyMappings = node.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }
    checkSemantics()
    nodeMapping.withPropertiesMapping(propertyMappings)

  }

  private def checkSemantics(): Unit = {
    ctx.semantics.typeMappings match {
      case types if types.nonEmpty =>
        nodeMapping.withNodeTypeMapping(types.head.value()) // @TODO: support multiple type mappings
      case _                       =>
        // ignore
    }
  }

  private def updateContext() = {
    ctx.registerNodeMapping(nodeMapping)
    ctx.genName(nodeMapping)
  }

}
