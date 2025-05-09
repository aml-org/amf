package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.common.parser.{
  Oas31ParameterParser,
  Oas3ServersParser,
  OasParameterParser,
  SecuritySchemeParser
}
import amf.apicontract.internal.spec.oas.parser.domain._
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry, YNode}

case class Oas31VersionFactory()(implicit override val ctx: OasWebApiContext) extends OasSpecVersionFactory {
  override def serversParser(map: YMap, api: Api): Oas3ServersParser =
    Oas3ServersParser(map, api, BaseApiModel.Servers)(ctx)

  override def serversParser(map: YMap, operation: Operation): Oas3ServersParser =
    Oas3ServersParser(map, operation, OperationModel.Servers)(ctx)

  override def serversParser(map: YMap, endpoint: EndPoint): OasServersParser =
    Oas3ServersParser(map, endpoint, EndPointModel.Servers)(ctx)

  override def securitySchemeParser: (YMapEntryLike, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Oas3SecuritySchemeParser.apply

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Oas31SecuritySettingsParser(map, scheme)(ctx)

  override def parameterParser(
      entryOrNode: YMapEntryLike,
      parentId: String,
      nameNode: Option[YNode],
      nameGenerator: IdCounter
  ): OasParameterParser =
    new Oas31ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)

  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    OasServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    Oas30OperationParser(entry, adopt)(ctx)

  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    Oas30EndpointParser(entry, parentId, collector)(ctx)
}
