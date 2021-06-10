package amf.plugins.parser.dialect

import amf.core.parser.Fields
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, UnionNodeMapping}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}

case class NodeShapeTransformer(node: NodeShape, ctx: ShapeTransformationContext) {

  val nodeMapping = NodeMapping(Fields(),node.annotations).withId(node.id)

  def transform(): NodeMapping = {
    updateContext()
    nameShape()
    val propertyMappings = node.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }
    checkInheritance()
    checkSemantics()
    nodeMapping.withPropertiesMapping(propertyMappings)

  }

  private def checkInheritance(): Unit = {
    val superSchemas = node.and
    if (superSchemas.length > 0) { // @TODO: support more than 1 super schema
      val hierarchy = superSchemas.map { case s: AnyShape =>
        val transformed = ShapeTransformer(s, ctx).transform()
        transformed match {
          case nm: NodeMapping       => nm.link[NodeMapping](nm.name.value())
          case unm: UnionNodeMapping => unm.link[UnionNodeMapping](unm.name.value())
        }
      }
      nodeMapping.withExtends(hierarchy)
    }
  }

  private def checkSemantics(): Unit = {
    ctx.semantics.typeMappings match {
      case types if types.nonEmpty =>
        nodeMapping.withNodeTypeMapping(types.head.value()) // @TODO: support multiple type mappings
      case _                       =>
        // ignore
    }
  }

  private def nameShape() {
    node.displayName.option() match {
      case Some(name) => nodeMapping.withName(name.replaceAll(" ", ""))
      case _          => ctx.genName(nodeMapping)
    }
  }
  private def updateContext(): Unit = {
    ctx.registerNodeMapping(nodeMapping)
  }

}
