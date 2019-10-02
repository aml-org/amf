package amf.plugins.document.webapi.contexts

import amf.plugins.document.webapi.parser.spec.domain.{Oas2ServersParser, Oas3ServersParser, OasServersParser}
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, WebApi}
import org.yaml.model.YMap

trait OasSpecAwareContext extends SpecAwareContext {}

trait OasSpecVersionFactory extends SpecVersionFactory {
  def serversParser(map: YMap, api: WebApi): OasServersParser
  def serversParser(map: YMap, endpoint: EndPoint): OasServersParser
  def serversParser(map: YMap, operation: Operation): OasServersParser
}

case class Oas2VersionFactory(implicit val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi) = Oas2ServersParser(map, api)

  override def serversParser(map: YMap, operation: Operation): OasServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)
}

case class Oas3VersionFactory(implicit val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi) = Oas3ServersParser(map, api, WebApiModel.Servers)

  override def serversParser(map: YMap, operation: Operation) =
    Oas3ServersParser(map, operation, OperationModel.Servers)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)
}
