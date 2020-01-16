package amf.plugins.document.webapi.parser.spec.declaration

import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.YPart

case class Async2SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: AsyncWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt)
