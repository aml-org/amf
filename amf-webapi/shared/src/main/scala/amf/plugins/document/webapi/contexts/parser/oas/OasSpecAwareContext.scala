package amf.plugins.document.webapi.contexts.parser.oas

import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{EndPoint, Operation, WebApi}
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

trait OasSpecAwareContext extends SpecAwareContext {}

abstract class OasSpecVersionFactory(implicit val ctx: OasWebApiContext) extends OasLikeSpecVersionFactory {
  def serversParser(map: YMap, api: WebApi): OasServersParser
  def serversParser(map: YMap, endpoint: EndPoint): OasServersParser
  def serversParser(map: YMap, operation: Operation): OasServersParser
  def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser
  def parameterParser(entryOrNode: YMapEntryLike,
                      parentId: String,
                      nameNode: Option[YNode],
                      nameGenerator: IdCounter): OasParameterParser
}

case class Oas2VersionFactory()(implicit override val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi): Oas2ServersParser = Oas2ServersParser(map, api)(ctx)

  override def serversParser(map: YMap, operation: Operation): OasServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)(ctx)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)(ctx)

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Oas2SecuritySettingsParser(map, scheme)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Oas2SecuritySchemeParser.apply

  override def parameterParser(entryOrNode: YMapEntryLike,
                               parentId: String,
                               nameNode: Option[YNode],
                               nameGenerator: IdCounter): OasParameterParser =
    Oas2ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)

  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    OasServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, producer: String => Operation): OasLikeOperationParser =
    Oas20OperationParser(entry, producer)(ctx)
  override def endPointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              collector: List[EndPoint]): OasLikeEndpointParser =
    Oas20EndpointParser(entry, producer, collector)(ctx)
}

case class Oas3VersionFactory()(implicit override val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: WebApi): Oas3ServersParser =
    Oas3ServersParser(map, api, WebApiModel.Servers)(ctx)

  override def serversParser(map: YMap, operation: Operation): Oas3ServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)(ctx)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Oas3SecuritySchemeParser.apply

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Oas3SecuritySettingsParser(map, scheme)(ctx)

  override def parameterParser(entryOrNode: YMapEntryLike,
                               parentId: String,
                               nameNode: Option[YNode],
                               nameGenerator: IdCounter): OasParameterParser =
    new Oas3ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)

  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    OasServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, producer: String => Operation): OasLikeOperationParser =
    Oas30OperationParser(entry, producer)(ctx)

  override def endPointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              collector: List[EndPoint]): OasLikeEndpointParser =
    Oas30EndpointParser(entry, producer, collector)(ctx)
}
