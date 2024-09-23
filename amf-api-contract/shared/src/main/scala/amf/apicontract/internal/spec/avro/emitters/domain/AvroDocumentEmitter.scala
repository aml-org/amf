package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.render.{EmptyRenderConfiguration, RenderConfiguration}
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.SpecOrdering.Lexical
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument

object AvroDocumentEmitter {
  def apply(renderConfig: RenderConfiguration) =
    new AvroDocumentEmitter(Lexical, renderConfig)(renderConfig.errorHandler)

  def apply(options: RenderOptions, errorHandler: AMFErrorHandler): AvroDocumentEmitter = {
    val renderConfig = EmptyRenderConfiguration(errorHandler, options)
    new AvroDocumentEmitter(Lexical, renderConfig)(errorHandler)
  }
}

case class AvroDocumentEmitter(ordering: SpecOrdering, renderConfig: RenderConfiguration)(implicit
    private val eh: AMFErrorHandler
) {

  def docLikeEmitter(root: Shape): YDocument = {
    val context     = new AvroShapeEmitterContext(eh, renderConfig)
    val rootEmitter = AvroShapeEmitter(root, ordering)(context).entries()

    val emitters = rootEmitter

    generateYDocument(emitters)
  }

  private def generateYDocument(emitters: Seq[EntryEmitter]): YDocument = {
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }
}
