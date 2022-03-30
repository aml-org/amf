package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.{AnyMapping, NodeMapping}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class AnyShapeTransformer(shape: AnyShape, ctx: ShapeTransformationContext)(
    implicit errorHandler: AMFErrorHandler)
    extends ShapeTransformer {

  val nodeMapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  def transform(): AnyMapping = {
    setMappingName(shape, nodeMapping)
    setMappingId(nodeMapping)
    updateContext(nodeMapping)
    transform(nodeMapping)
  }

  def transform[T <: AnyMapping](mapping: T): T = {
    val andMembers = shape.and.map {
      case member: AnyShape =>
        val transformed = ShapeTransformation(member, ctx).transform()
        transformed.id
    }
    if (andMembers.nonEmpty) mapping.withAnd(andMembers)

    val orMembers = shape.or.map {
      case member: AnyShape =>
        val transformed = ShapeTransformation(member, ctx).transform()
        transformed.id
    }
    if (orMembers.nonEmpty) mapping.withOr(orMembers)

    Option(shape.ifShape).foreach {
      case ifShape: AnyShape =>
        val transformed = ShapeTransformation(ifShape, ctx).transform()
        mapping.withIfMapping(transformed.id)
        val transformedThen = Option(shape.thenShape) match {
          case Some(thenShape) if thenShape.isInstanceOf[AnyShape] =>
            val transformed = ShapeTransformation(thenShape.asInstanceOf[AnyShape], ctx).transform()
            transformed
          case None =>
            NodeMapping(shape.annotations).withId(mapping.id + "/then")
        }
        mapping.withThenMapping(transformedThen.id)

        val transformedElse = Option(shape.elseShape) match {
          case Some(elseShape) if elseShape.isInstanceOf[AnyShape] =>
            val transformed = ShapeTransformation(elseShape.asInstanceOf[AnyShape], ctx).transform()
            transformed
          case None => NodeMapping(shape.annotations).withId(mapping.id + "/else")
        }
        mapping.withElseMapping(transformedElse.id)
      case _ => // ignore
    }
    mapping
  }
}

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

    AnyShapeTransformer(shape, ctx).transform(nodeMapping)

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
