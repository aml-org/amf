package amf.plugins.document.apicontract.parser.spec.declaration

import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import org.yaml.model.YPart

case class Async2SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: AsyncWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt)
