package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser.domain
import amf.apicontract.internal.spec.oas.parser.domain._
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

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

  override def parameterParser(
      entryOrNode: YMapEntryLike,
      parentId: String,
      nameNode: Option[YNode],
      nameGenerator: IdCounter
  ): OasParameterParser =
    Oas2ParameterParser(entryOrNode, parentId, nameNode, nameGenerator)(ctx)

  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    domain.OasServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    Oas20OperationParser(entry, adopt)(ctx)
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    Oas20EndpointParser(entry, parentId, collector)(ctx)
}
