package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.annotations.ExplicitField
import amf.core.emitter.BaseEmitters.{EntryPartEmitter, RawEmitter}
import amf.core.emitter.EntryEmitter
import amf.core.model.domain.Shape
import amf.core.parser.FieldEntry
import org.yaml.model.YType

case class RamlRequiredShapeEmitter(shape: Shape, minCount: Option[FieldEntry]) {

  def emitter(): Option[EntryEmitter] = {
    minCount.flatMap { entry =>
      if (entry.value.annotations.contains(classOf[ExplicitField])) {
        Some(
          EntryPartEmitter("required",
                           RawEmitter(if (entry.scalar.toNumber.intValue() > 0) "true" else "false", YType.Bool)))
      } else {
        None
      }
    }
  }
}
