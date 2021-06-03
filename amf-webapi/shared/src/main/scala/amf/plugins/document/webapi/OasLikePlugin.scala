package amf.plugins.document.webapi
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document._
import amf.core.parser.DefaultReferenceCollector
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext

import scala.collection.mutable

trait OasLikePlugin extends BaseWebApiPlugin {

  override def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): OasLikeSpecEmitterContext

}
