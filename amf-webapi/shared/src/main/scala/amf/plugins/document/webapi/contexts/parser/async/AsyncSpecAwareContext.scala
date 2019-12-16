package amf.plugins.document.webapi.contexts.parser.async

import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory(ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {}
