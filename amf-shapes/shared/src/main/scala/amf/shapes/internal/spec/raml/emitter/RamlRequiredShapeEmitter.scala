package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, RawEmitter}
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YType

case class RamlRequiredShapeEmitter(shape: Shape, minCount: Option[FieldEntry]) {

  def emitter(): Option[EntryEmitter] = {
    minCount.flatMap { entry =>
      if (entry.value.annotations.contains(classOf[ExplicitField])) {
        Some(
          EntryPartEmitter(
            "required",
            RawEmitter(if (entry.scalar.toNumber.intValue() > 0) "true" else "false", YType.Bool)
          )
        )
      } else {
        None
      }
    }
  }
}
