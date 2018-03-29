package amf.plugins.document.webapi.contexts

import amf.plugins.document.webapi.parser.spec.domain.{Oas2ServersParser, Oas3ServersParser, OasServersParser}
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.YMap

trait OasSpecAwareContext extends SpecAwareContext {}

trait OasSpecVersionFactory extends SpecVersionFactory {
  def serversParser(map: YMap, api: WebApi): OasServersParser
}

case class Oas2VersionFactory(implicit val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi) = Oas2ServersParser(map, api)
}

case class Oas3VersionFactory(implicit val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi) = Oas3ServersParser(map, api)
}
