package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser._
import amf.plugins.document.webapi.contexts.CustomClosedShapeContextDecorator
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.oas.OasCustomSyntax
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.{YMap, YPart}

object Oas2SecuritySchemeParser {
  def apply(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasLikeWebApiContext) =
    new Oas2SecuritySchemeParser(part, adopt)(new CustomClosedShapeContextDecorator(ctx, OasCustomSyntax))
}

case class Oas2SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasLikeWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt) {
  override def closedShape(scheme: SecurityScheme, map: YMap, shape: String): Unit = {

    val key = getSchemeType(map).getOrElse(shape)
    ctx.closedShape(scheme.id, map, key)
    filterFlow(map).foreach(ctx.closedShape(scheme.id, map, _))
  }

  private def filterFlow(map: YMap) = map.key("flow").map(_.value.as[String]) match {
    case Some(v @ ("implicit" | "accessCode" | "application" | "password")) => Some(v)
    case Some(_)                                                            => None
    case None                                                               => None
  }

  private def getSchemeType(map: YMap) = map.key("type").map(_.value.as[String]).collect {
    case schemeType @ ("basic" | "oauth2" | "apiKey") => schemeType
  }
}

case class Oas3SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasLikeWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt) {}
