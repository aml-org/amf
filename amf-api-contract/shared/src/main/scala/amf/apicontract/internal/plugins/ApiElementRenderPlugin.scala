package amf.apicontract.internal.plugins

import amf.apicontract.internal.spec.common.emitter.DomainElementEmitterFactory
import amf.core.client.common.position.Position
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.parse.document.{ParsedDocument, SyamlParsedDocument}
import amf.core.client.scala.render.AMFElementRenderPlugin
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.emitters.PartEmitter
import org.yaml.model.YDocument
import org.yaml.model.YDocument.PartBuilder

trait ApiElementRenderPlugin extends AMFElementRenderPlugin {
  override val id: String = s"${spec.id} Element"

  protected def spec: Spec
  protected def emitterFactory: (AMFErrorHandler, RenderConfiguration) => DomainElementEmitterFactory

  override def applies(element: DomainElement, config: RenderConfiguration): Boolean =
    emitterFactory(IgnoringErrorHandler, config).emitter(element).isDefined

  override def render(
      element: DomainElement,
      errorHandler: AMFErrorHandler,
      config: RenderConfiguration
  ): ParsedDocument = {
    val emitter = emitterFactory(errorHandler, config).emitter(element).getOrElse(new EmptyEmitter())
    val document = YDocument { b =>
      traverse(Seq(emitter), b)
    }
    SyamlParsedDocument(document)
  }
}

class EmptyEmitter extends PartEmitter {
  override def emit(b: PartBuilder): Unit = b += ""

  override def position(): Position = Position.ZERO
}
