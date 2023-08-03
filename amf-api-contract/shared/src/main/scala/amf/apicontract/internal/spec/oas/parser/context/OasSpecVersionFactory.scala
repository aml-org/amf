package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.common.parser.OasParameterParser
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeSecuritySettingsParser, OasServersParser}
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YNode}

abstract class OasSpecVersionFactory(implicit val ctx: OasWebApiContext) extends OasLikeSpecVersionFactory {
  def serversParser(map: YMap, api: Api): OasServersParser
  def serversParser(map: YMap, endpoint: EndPoint): OasServersParser
  def serversParser(map: YMap, operation: Operation): OasServersParser
  def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser
  def parameterParser(
      entryOrNode: YMapEntryLike,
      parentId: String,
      nameNode: Option[YNode],
      nameGenerator: IdCounter
  ): OasParameterParser
}
