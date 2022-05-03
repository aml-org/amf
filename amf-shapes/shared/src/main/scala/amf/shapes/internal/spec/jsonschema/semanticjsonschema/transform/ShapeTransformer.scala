package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.domain.NodeMappable
import amf.aml.internal.metamodel.domain.NodeMappableModel
import amf.core.client.scala.model.domain.Shape

trait ShapeTransformer {

  protected val ctx: ShapeTransformationContext

  protected def setMappingName[T <: NodeMappableModel](shape: Shape, mapping: NodeMappable[T]): Unit = {
    val maxLength = 60
    shape.displayName.option() match {
      case Some(name) =>
        val nameMaxLength = if (name.length <= maxLength) name.length else maxLength
        mapping.withName(name.substring(0, nameMaxLength).replaceAll("[^a-zA-Z0-9]", ""))
      case _ => ctx.genName(mapping)
    }
  }

  protected def setMappingId[T <: NodeMappableModel](mapping: NodeMappable[T]): Unit = {
    val id = s"#/declarations/${mapping.name}"
    mapping.withId(id)
  }

  protected def updateContext[T <: NodeMappableModel](mapping: NodeMappable[T]): Unit = {
    ctx.registerNodeMapping(mapping)
  }
}
