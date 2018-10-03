package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.remote.{Oas, Raml}
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.{RamlWebApiContext, WebApiContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security.{Scope, SecurityScheme, Settings}
import amf.plugins.domain.webapi.models.{Parameter, Response}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model._

import scala.collection.mutable

/**
  *
  */
object SecuritySchemeParser {
  def apply(entry: YMapEntry, adopt: (SecurityScheme, String) => SecurityScheme)(
      implicit ctx: WebApiContext): SecuritySchemeParser = // todo factory for oas too?
    ctx.vendor match {
      case _: Raml => RamlSecuritySchemeParser(entry, entry.key.as[YScalar].text, entry.value, adopt)(toRaml(ctx))
      case _: Oas  => OasSecuritySchemeParser(entry, entry.key, entry.value, adopt)
      case other   => throw new IllegalArgumentException(s"Unsupported vendor $other in security scheme parsers")
    }

}

trait SecuritySchemeParser extends SpecParserOps {
  def parse(): SecurityScheme
}
case class RamlSecuritySchemeParser(ast: YPart,
                                    key: String,
                                    node: YNode,
                                    adopt: (SecurityScheme, String) => SecurityScheme)(implicit ctx: RamlWebApiContext)
    extends SecuritySchemeParser {
  override def parse(): SecurityScheme = {
    ctx.link(node) match {
      case Left(link) => parseReferenced(key, link, Annotations(node), adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(ast), key)

        val map = value.as[YMap]

        map.key("type", (SecuritySchemeModel.Type in scheme).allowingAnnotations)
        map.key("displayName", (SecuritySchemeModel.DisplayName in scheme).allowingAnnotations)
        map.key("description", (SecuritySchemeModel.Description in scheme).allowingAnnotations)

        RamlDescribedByParser("describedBy", map, scheme).parse()

        map.key("settings", SecuritySchemeModel.Settings in scheme using RamlSecuritySettingsParser.parse(scheme))

        AnnotationParser(scheme, map).parse()

        scheme
    }
  }

  def parseReferenced(name: String,
                      parsedUrl: String,
                      annotations: Annotations,
                      adopt: (SecurityScheme, String) => SecurityScheme): SecurityScheme = {
    val scheme = ctx.declarations
      .findSecuritySchemeOrError(ast)(parsedUrl, SearchScope.All)

    val copied: SecurityScheme = scheme.link(parsedUrl, annotations)
    adopt(copied, name)
    copied.withName(name)
  }
}

case class RamlDescribedByParser(key: String, map: YMap, scheme: SecurityScheme)(implicit ctx: RamlWebApiContext) {
  def parse(): Unit = {
    map.key(
      key,
      entry => {
        entry.value.toOption[YMap] match {
          case Some(value) =>
            value.key(
              "headers",
              entry => {
                val parameters: Seq[Parameter] =
                  RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(scheme.id)) // todo replace in separation
                    .parse()
                    .map(_.withBinding("header"))
                scheme.set(SecuritySchemeModel.Headers,
                           AmfArray(parameters, Annotations(entry.value)),
                           Annotations(entry))
              }
            )

            if (map.key("queryParameters").isDefined && map.key("queryString").isDefined) {
              ctx.violation(
                ParserSideValidations.ExclusivePropertiesSpecification.id,
                scheme.id,
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together",
                map
              )
            }

            value.key(
              "queryParameters",
              entry => {
                val parameters: Seq[Parameter] =
                  RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(scheme.id)) // todo replace in separation
                    .parse()
                    .map(_.withBinding("query"))
                scheme.set(SecuritySchemeModel.QueryParameters,
                           AmfArray(parameters, Annotations(entry.value)),
                           Annotations(entry))
              }
            )

            value.key(
              "queryString",
              queryEntry => {
                Raml10TypeParser(queryEntry, shape => shape.adopted(scheme.id))
                  .parse()
                  .foreach(s => scheme.withQueryString(tracking(s, scheme.id)))
              }
            )

            value.key(
              "responses",
              entry => {
                entry.value
                  .as[YMap]
                  .regex(
                    "\\d{3}",
                    entries => {
                      val responses = mutable.ListBuffer[Response]()
                      entries.foreach(entry => {
                        responses += ctx.factory
                          .responseParser(entry, (r: Response) => r.adopted(scheme.id), false)
                          .parse() // todo replace in separation
                      })
                      scheme.set(SecuritySchemeModel.Responses,
                                 AmfArray(responses, Annotations(entry.value)),
                                 Annotations(entry))
                    }
                  )
              }
            )
          case _ => // should add some warning or violation for secs ?
        }
      }
    )
  }
}

case class OasSecuritySchemeParser(ast: YPart,
                                   key: String,
                                   node: YNode,
                                   adopt: (SecurityScheme, String) => SecurityScheme)(implicit ctx: WebApiContext)
    extends SecuritySchemeParser {
  def parse(): SecurityScheme = {
    ctx.link(node) match {
      case Left(link) => parseReferenced(key, link, Annotations(node), adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(ast), key)

        val map = value.as[YMap]

        map.key(
          "type",
          entry => {
            val t: String = entry.value.as[YScalar].text match {
              case "oauth2" => "OAuth 2.0"
              case "basic"  => "Basic Authentication"
              case "apiKey" => "apiKey".asOasExtension
              case s        => s
            }

            scheme.set(SecuritySchemeModel.Type, AmfScalar(t, Annotations(entry.value)), Annotations(entry))
          }
        )

        map.key("displayName".asOasExtension, SecuritySchemeModel.DisplayName in scheme)
        map.key("description", SecuritySchemeModel.Description in scheme)

        RamlDescribedByParser("describedBy".asOasExtension, map, scheme)(toRaml(ctx)).parse()

        OasSecuritySettingsParser(map, scheme)
          .parse()
          .foreach(scheme.set(SecuritySchemeModel.Settings, _, Annotations(ast)))

        AnnotationParser(scheme, map).parse()

        scheme
    }
  }

  case class OasSecuritySettingsParser(map: YMap, scheme: SecurityScheme) {
    def parse(): Option[Settings] = {
      val result = scheme.`type`.value() match {
        case "OAuth 1.0"    => Some(oauth1())
        case "OAuth 2.0"    => Some(oauth2())
        case "x-amf-apiKey" => Some(apiKey())
        case _ =>
          map
            .key("settings".asOasExtension)
            .map(entry => dynamicSettings(entry.value.as[YMap], scheme.withDefaultSettings()))
      }

      result.map(ss => {
        AnnotationParser(ss, map).parse()
        ss.add(Annotations(map))
      })
    }

    def dynamicSettings(xSettings: YMap, settings: Settings, properties: String*): Settings = {
      val entries = xSettings.entries.filterNot { entry =>
        val key: String = entry.key.as[YScalar].text
        properties.contains(key) || isOasAnnotation(key)
      }

      if (entries.nonEmpty) {
        val node = DataNodeParser(YNode(YMap(entries, entries.headOption.map(_.sourceName).getOrElse(""))),
                                  parent = Some(settings.id)).parse()
        settings.set(SettingsModel.AdditionalProperties, node)
      }

      AnnotationParser(scheme, xSettings).parse()

      settings
    }

    private def apiKey() = {
      val settings = scheme.withApiKeySettings()

      map.key("name", entry => {
        val value = ScalarNode(entry.value)
        settings.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
      })

      map.key("in", entry => {
        val value = ScalarNode(entry.value)
        settings.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
      })

      map.key(
        "settings".asOasExtension,
        entry => dynamicSettings(entry.value.as[YMap], settings, "name", "in")
      )

      settings
    }

    private def oauth2() = {
      val settings = scheme.withOAuth2Settings()

      map.key("authorizationUrl", OAuth2SettingsModel.AuthorizationUri in settings)
      map.key("tokenUrl", OAuth2SettingsModel.AccessTokenUri in settings)

      // TODO we should find similarity between raml authorizationGrants and this to map between values.
      map.key(
        "flow",
        entry => {
          val value = ScalarNode(entry.value)
          settings.set(OAuth2SettingsModel.Flow, value.string(), Annotations(entry))
        }
      )

      map.key(
        "scopes",
        entry => {
          val scopeMap = entry.value.as[YMap]
          val scopes =
            scopeMap.entries.filterNot(entry => isOasAnnotation(entry.key)).map(parseScope)
          settings.setArray(OAuth2SettingsModel.Scopes, scopes, Annotations(entry))
        }
      )

      map.key(
        "settings".asOasExtension,
        entry => {
          val xSettings = entry.value.as[YMap]

          xSettings.key("authorizationGrants", OAuth2SettingsModel.AuthorizationGrants in settings)

          dynamicSettings(xSettings, settings, "authorizationGrants")
        }
      )

      AnnotationParser(settings, map).parseOrphanNode("scopes")

      settings
    }

    private def parseScope(scopeEntry: YMapEntry) = {
      val name: String        = scopeEntry.key.as[YScalar].text
      val description: String = scopeEntry.value

      Scope(scopeEntry)
        .set(ScopeModel.Name, AmfScalar(name), Annotations(scopeEntry.key))
        .set(ScopeModel.Description, AmfScalar(description), Annotations(scopeEntry.value))
    }

    private def oauth1() = {
      val settings = scheme.withOAuth1Settings()

      map.key(
        "settings".asOasExtension,
        entry => {
          val map = entry.value.as[YMap]

          map.key("requestTokenUri", OAuth1SettingsModel.RequestTokenUri in settings)
          map.key("authorizationUri", OAuth1SettingsModel.AuthorizationUri in settings)
          map.key("tokenCredentialsUri", OAuth1SettingsModel.TokenCredentialsUri in settings)
          map.key("signatures", OAuth1SettingsModel.Signatures in settings)

          dynamicSettings(map, settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
        }
      )

      settings
    }
  }

  def parseReferenced(name: String,
                      parsedUrl: String,
                      annotations: Annotations,
                      adopt: (SecurityScheme, String) => SecurityScheme): SecurityScheme = {
    val scheme = ctx.declarations
      .findSecuritySchemeOrError(ast)(parsedUrl, SearchScope.Fragments)

    val copied: SecurityScheme = scheme.link(parsedUrl, annotations)
    adopt(copied, name)
    copied.withName(name)
  }
}
