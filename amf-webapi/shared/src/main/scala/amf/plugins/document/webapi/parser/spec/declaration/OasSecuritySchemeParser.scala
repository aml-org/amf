package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser._
import amf.core.validation.SeverityLevels
import amf.plugins.document.webapi.contexts.CustomClosedShapeContextDecorator
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.SpecSyntax
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.{YMap, YPart}

object OasSecuritySchemeParser {
  def apply(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasLikeWebApiContext) =
    new OasSecuritySchemeParser(part, adopt)(
      new CustomClosedShapeContextDecorator(ctx, Oas2SecuritySchemeSyntax, severities))

  object Oas2SecuritySchemeSyntax extends SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map(
      "basic" -> Set(
        "description",
        "type"
      )
    )
  }

  val severities = Map(
    "basic" -> SeverityLevels.WARNING
  )
}

case class OasSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasLikeWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt) {
  override def closedShape(scheme: SecurityScheme, map: YMap, shape: String): Unit = {
    val key = map.key("type").map(_.value.as[String]) match {
      case Some("basic") => "basic"
      case _             => shape
    }
    ctx.closedShape(scheme.id, map, key)
  }
}
