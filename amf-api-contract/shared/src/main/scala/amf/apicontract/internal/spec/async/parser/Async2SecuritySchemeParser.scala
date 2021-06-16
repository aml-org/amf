package amf.apicontract.internal.spec.async.parser

import amf.apicontract.internal.spec.oas.parser.OasLikeSecuritySchemeParser
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import org.yaml.model.YPart

case class Async2SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: AsyncWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt)
