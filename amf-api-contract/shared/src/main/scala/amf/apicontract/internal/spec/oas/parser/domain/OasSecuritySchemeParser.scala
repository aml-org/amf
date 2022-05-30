package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.spec.oas.parser.context.{
  CustomClosedShapeContextDecorator,
  OasCustomSyntax,
  OasLikeWebApiContext
}
import amf.core.internal.parser.YMapOps
import org.yaml.model.{YMap, YPart}

object Oas2SecuritySchemeParser {
  def apply(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasLikeWebApiContext) =
    new Oas2SecuritySchemeParser(part, adopt)(new CustomClosedShapeContextDecorator(ctx, OasCustomSyntax))
}

case class Oas2SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit
    ctx: OasLikeWebApiContext
) extends OasLikeSecuritySchemeParser(part, adopt) {
  override def closedShape(scheme: SecurityScheme, map: YMap, shape: String): Unit = {

    val key = getSchemeType(map).getOrElse(shape)
    ctx.closedShape(scheme, map, key)
    filterFlow(map).foreach(ctx.closedShape(scheme, map, _))
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

case class Oas3SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit
    ctx: OasLikeWebApiContext
) extends OasLikeSecuritySchemeParser(part, adopt) {}
