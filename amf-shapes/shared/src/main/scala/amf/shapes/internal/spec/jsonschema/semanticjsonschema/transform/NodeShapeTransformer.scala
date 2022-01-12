package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{NodeMapping, UnionNodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.Fields
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class NodeShapeTransformer(shape: NodeShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler) {

  val nodeMapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def transform(): NodeMapping = {
    setMappingName()
    setMappingID()
    updateContext()

    val propertyMappings = shape.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }

    checkInheritance()
    checkSemantics()
    nodeMapping.withPropertiesMapping(propertyMappings)

  }

  private def checkInheritance(): Unit = {
    val superSchemas = shape.and
    if (superSchemas.nonEmpty) { // @TODO: support more than 1 super schema
      val hierarchy = superSchemas.map {
        case s: AnyShape =>
          val transformed = ShapeTransformation(s, ctx).transform()
          transformed match {
            case nm: NodeMapping       => nm.link(nm.name.value())
            case unm: UnionNodeMapping => unm.link(unm.name.value())
          }
      }
      nodeMapping.withExtends(hierarchy)
    }
  }

  private def checkSemantics(): Unit = {
    ctx.semantics.typeMappings match {
      case types if types.nonEmpty =>
        nodeMapping.withNodeTypeMapping(types.head.value()) // @TODO: support multiple type mappings
      case _ =>
      // ignore
    }
  }

  private def setMappingName(): Unit = {
    shape.displayName.option() match {
      case Some(name) => nodeMapping.withName(name.replaceAll(" ", ""))
      case _          => ctx.genName(nodeMapping)
    }
  }

  private def setMappingID(): Unit = {
    val id = s"#/declarations/${nodeMapping.name}"
    nodeMapping.withId(id)
  }

  private def updateContext(): Unit = {
    ctx.registerNodeMapping(nodeMapping)
  }
}
