package amf.apicontract.internal.spec.avro.emitters.context

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.render.RenderConfiguration
import amf.shapes.internal.annotations.AVROSchemaType

class AvroShapeEmitterContext(
    eh: AMFErrorHandler,
    config: RenderConfiguration
) extends AvroSpecEmitterContext(eh, AvroRefEmitter, config) {

  override val factory: AvroSpecEmitterFactory = new AvroSpecEmitterFactory()(this)

  def getAvroType(shape: Shape): Option[String] = {
    shape.annotations.find(classOf[AVROSchemaType]).map(_.avroType)
  }

  def isPrimitive(avroType: String): Boolean =
    Seq("null", "boolean", "int", "long", "float", "double", "bytes", "string").contains(avroType)

  def isComplex(avroType: String): Boolean =
    Seq("record", "enum", "array", "map", "union", "fixed").contains(avroType)

  def isComplex(shape: Shape): Boolean = getAvroType(shape).exists(isComplex)

  def isPrimitive(shape: Shape): Boolean = getAvroType(shape).exists(isPrimitive)
}

object AvroShapeEmitterContext {
  implicit def fromSpecEmitterContext(implicit spec: AvroSpecEmitterContext): AvroShapeEmitterContext =
    new AvroShapeEmitterContext(spec.eh, spec.config)

  def apply(eh: AMFErrorHandler, config: RenderConfiguration): AvroShapeEmitterContext = {
    new AvroShapeEmitterContext(eh, config)
  }
}
