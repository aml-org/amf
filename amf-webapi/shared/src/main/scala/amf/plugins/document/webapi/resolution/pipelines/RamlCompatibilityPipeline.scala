package amf.plugins.document.webapi.resolution.pipelines

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}

class RamlCompatibilityPipeline(override val eh: ErrorHandler) extends Raml10ResolutionPipeline(eh) {

  // Todo add step for compatibility

}

object RamlCompatibilityPipeline {
  def unhandled = new RamlCompatibilityPipeline(UnhandledErrorHandler)
}
