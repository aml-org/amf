package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser._
import amf.core.validation.SeverityLevels
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.{CustomClosedShapeContextDecorator, SpecField, SpecNode}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.{YMap, YPart}

object Oas2SecuritySchemeParser {
  def apply(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: OasLikeWebApiContext) =
    new Oas2SecuritySchemeParser(part, adopt)(new CustomClosedShapeContextDecorator(ctx, Oas2SecuritySchemeNodes))

  val flowPossibleFields = Set(
    "type",
    "description",
    "flow",
    "scopes"
  )

  val Oas2SecuritySchemeNodes = Map(
    "basic" -> SpecNode(
      possibleFields = Set("type", "description")
    ),
    "oauth2" -> SpecNode(
      requiredFields = Set(
        SpecField("flow", SeverityLevels.WARNING),
        SpecField("scopes", SeverityLevels.WARNING)
      ),
      possibleFields = Set(
        "type",
        "description",
        "authorizationUrl",
        "tokenUrl",
        "flow",
        "scopes",
      )
    ),
    "implicit" -> SpecNode(
      requiredFields = Set(SpecField("authorizationUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    ),
    "accessCode" -> SpecNode(
      requiredFields = Set(
        SpecField("authorizationUrl", SeverityLevels.WARNING),
        SpecField("tokenUrl", SeverityLevels.WARNING)
      ),
      possibleFields = flowPossibleFields
    ),
    "application" -> SpecNode(
      requiredFields = Set(SpecField("tokenUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    ),
    "password" -> SpecNode(
      requiredFields = Set(SpecField("tokenUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    )
  )
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
    case schemeType @ ("basic" | "oauth2") => schemeType
  }
}

case class Oas3SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: OasLikeWebApiContext)
    extends OasLikeSecuritySchemeParser(part, adopt) {}
