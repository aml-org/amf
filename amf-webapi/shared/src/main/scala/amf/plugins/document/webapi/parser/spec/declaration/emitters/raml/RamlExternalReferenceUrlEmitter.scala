package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.PartEmitter
import amf.core.model.domain.Shape
import amf.core.parser.Position
import org.yaml.model.{YDocument, YNode}
import amf.core.emitter.BaseEmitters.pos
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext

// emit !include ref for json schemas that have been inlined
case class RamlExternalReferenceUrlEmitter(shape: Shape)(implicit spec: RamlSpecEmitterContext) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit =
    shape.annotations.find(classOf[ExternalReferenceUrl]).foreach {
      case ExternalReferenceUrl(url) => b += YNode.include(url)
      case _                         =>
    }

  override def position(): Position = pos(shape.annotations)
}
