package amf.plugins.document.webapi.contexts.parser.oas

import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.declaration.{
  Oas2SecuritySettingsParser,
  Oas3SecuritySettingsParser,
  OasSecuritySettingsParser
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{EndPoint, Operation, WebApi}
import org.yaml.model.{YMap, YMapEntry, YNode}

trait OasSpecAwareContext extends SpecAwareContext {}

trait OasSpecVersionFactory extends OasLikeSpecVersionFactory {
  def serversParser(map: YMap, api: WebApi): OasServersParser
  def serversParser(map: YMap, endpoint: EndPoint): OasServersParser
  def serversParser(map: YMap, operation: Operation): OasServersParser
  def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasSecuritySettingsParser
  def parameterParser(entryOrNode: Either[YMapEntry, YNode],
                      parentId: String,
                      nameNode: Option[YNode],
                      nameGenerator: IdCounter): OasParameterParser
}

case class Oas2VersionFactory(ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi): Oas2ServersParser = Oas2ServersParser(map, api)(ctx)

  override def serversParser(map: YMap, operation: Operation): OasServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)(ctx)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)(ctx)
  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasSecuritySettingsParser =
    Oas2SecuritySettingsParser(map, scheme)(ctx)

  override def parameterParser(entryOrNode: Either[YMapEntry, YNode],
                               parentId: String,
                               nameNode: Option[YNode],
                               nameGenerator: IdCounter): OasParameterParser =
    Oas2ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)
}

case class Oas3VersionFactory(ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi): Oas3ServersParser =
    Oas3ServersParser(map, api, WebApiModel.Servers)(ctx)

  override def serversParser(map: YMap, operation: Operation): Oas3ServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)(ctx)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)(ctx)

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasSecuritySettingsParser =
    new Oas3SecuritySettingsParser(map, scheme)(ctx)

  override def parameterParser(entryOrNode: Either[YMapEntry, YNode],
                               parentId: String,
                               nameNode: Option[YNode],
                               nameGenerator: IdCounter): OasParameterParser =
    new Oas3ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)
}
