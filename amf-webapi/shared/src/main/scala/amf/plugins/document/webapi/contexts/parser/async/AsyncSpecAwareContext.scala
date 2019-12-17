package amf.plugins.document.webapi.contexts.parser.async

import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.domain.{OasLikeServerVariableParser, AsyncServerVariableParser}
import amf.plugins.domain.webapi.models.Server
import org.yaml.model.YMapEntry

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory(ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, server: Server): OasLikeServerVariableParser =
    domain.AsyncServerVariableParser(entry, server)(ctx)
}
