package amf.plugins.document.webapi.parser.spec.declaration

import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.YPart

case class OasSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt)
