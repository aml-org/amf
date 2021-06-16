package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.common.emitter.SpecAwareContext
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser
import amf.apicontract.internal.spec.oas.parser._
import amf.core.internal.utils.IdCounter
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

trait OasSpecAwareContext extends SpecAwareContext {}

abstract class OasSpecVersionFactory(implicit val ctx: OasWebApiContext) extends OasLikeSpecVersionFactory {
  def serversParser(map: YMap, api: Api): OasServersParser
  def serversParser(map: YMap, endpoint: EndPoint): OasServersParser
  def serversParser(map: YMap, operation: Operation): OasServersParser
  def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser
  def parameterParser(entryOrNode: YMapEntryLike,
                      parentId: String,
                      nameNode: Option[YNode],
                      nameGenerator: IdCounter): OasParameterParser
}

case class Oas2VersionFactory()(implicit override val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: Api): Oas2ServersParser = Oas2ServersParser(map, api)(ctx)

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

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    Oas20OperationParser(entry, adopt)(ctx)
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    Oas20EndpointParser(entry, parentId, collector)(ctx)
}

case class Oas3VersionFactory()(implicit override val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: Api): Oas3ServersParser =
    Oas3ServersParser(map, api, BaseApiModel.Servers)(ctx)

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
    parser.OasServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    Oas30OperationParser(entry, adopt)(ctx)

  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    Oas30EndpointParser(entry, parentId, collector)(ctx)
}
