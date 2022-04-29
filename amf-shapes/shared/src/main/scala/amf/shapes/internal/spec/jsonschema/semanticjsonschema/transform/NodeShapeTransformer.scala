package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMapping
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.client.scala.model.domain.NodeShape

class NodeShapeTransformer(shape: NodeShape, override val ctx: ShapeTransformationContext)(implicit eh: AMFErrorHandler)
    extends AnyShapeTransformer(shape, ctx)
    with ShapeTransformer {

  override val mapping: NodeMapping = NodeMapping(shape.annotations).withId(shape.id)

  override def transform(): NodeMapping = {

    super.transform()

    val propertyMappings = shape.properties.map { property =>
      PropertyShapeTransformer(property, ctx).transform()
    }
    if (propertyMappings.nonEmpty) mapping.withPropertiesMapping(propertyMappings)

    checkAdditionalProperties()
    checkSemantics()
    mapping

  }

  private def checkAdditionalProperties() = {
    shape.closed.option() match {
      case Some(value) => mapping.withClosed(value)
      case None        => mapping.withClosed(false)
    }
  }

  private def checkSemantics(): Unit = {
    // @TODO: support multiple type mappings
    ctx.semantics.typeMappings.headOption
      .flatMap(_.option())
      .foreach { typeMapping =>
        mapping.withNodeTypeMapping(typeMapping)
      }
  }
}

object NodeShapeTransformer {
  def apply(shape: NodeShape, ctx: ShapeTransformationContext)(implicit errorHandler: AMFErrorHandler) =
    new NodeShapeTransformer(shape, ctx)
}
