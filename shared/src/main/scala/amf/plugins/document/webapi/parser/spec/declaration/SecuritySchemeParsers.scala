package amf.plugins.document.webapi.parser.spec.declaration

import amf.framework.model.domain.{AmfArray, AmfScalar}
import amf.framework.parser.{Annotations, _}
import amf.framework.remote.{Oas, Raml}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.{SearchScope, _}
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.domain.{RamlParametersParser, RamlResponseParser, RamlSecuritySettingsParser}
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security.{Scope, SecurityScheme, Settings}
import amf.plugins.domain.webapi.models.{Parameter, Response}
import org.yaml.model._

import scala.collection.mutable

/**
  *
  */
object SecuritySchemeParser {
  def apply(entry: YMapEntry, adopt: (SecurityScheme) => SecurityScheme)(
      implicit ctx: WebApiContext): SecuritySchemeParser =
    ctx.vendor match {
      case Raml  => RamlSecuritySchemeParser(entry, entry.key, entry.value, adopt)
      case Oas   => OasSecuritySchemeParser(entry, entry.key, entry.value, adopt)
      case other => throw new IllegalArgumentException(s"Unsupported vendor $other in security scheme parsers")
    }

}

trait SecuritySchemeParser {
  def parse(): SecurityScheme
}
case class RamlSecuritySchemeParser(ast: YPart, key: String, node: YNode, adopt: (SecurityScheme) => SecurityScheme)(
    implicit ctx: WebApiContext)
    extends SecuritySchemeParser {
  override def parse(): SecurityScheme = {
    ctx.link(node) match {
      case Left(link) => parseReferenced(key, link, Annotations(node))
      case Right(value) =>
        val scheme = adopt(SecurityScheme(ast))

        val map = value.as[YMap]

        map.key("type", entry => {
          val value = ValueNode(entry.value)
          scheme.set(SecuritySchemeModel.Type, value.string(), Annotations(entry))
        })

        map.key("displayName", entry => {
          val value = ValueNode(entry.value)
          scheme.set(SecuritySchemeModel.DisplayName, value.string(), Annotations(entry))
        })

        map.key("description", entry => {
          val value = ValueNode(entry.value)
          scheme.set(SecuritySchemeModel.Description, value.string(), Annotations(entry))
        })

        RamlDescribedByParser("describedBy", map, scheme).parse()

        map.key(
          "settings",
          entry => {
            val settings = RamlSecuritySettingsParser(entry.value.as[YMap], scheme.`type`, scheme).parse()

            scheme.set(SecuritySchemeModel.Settings, settings, Annotations(entry))
          }
        )

        AnnotationParser(() => scheme, map).parse()

        scheme
    }
  }

  def parseReferenced(name: String, parsedUrl: String, annotations: Annotations): SecurityScheme = {
    val scheme = ctx.declarations
      .findSecuritySchemeOrError(ast)(parsedUrl, SearchScope.Fragments)

    val copied: SecurityScheme = scheme.link(parsedUrl, annotations)
    copied.withName(name)
  }
}

object RamlDescribedByParser {
  def apply(key: String, map: YMap, scheme: SecurityScheme)(implicit ctx: WebApiContext): RamlDescribedByParser =
    new RamlDescribedByParser(key, map, scheme)(toRaml(ctx))
}

case class RamlDescribedByParser(key: String, map: YMap, scheme: SecurityScheme)(implicit ctx: WebApiContext) {
  def parse(): Unit = {
    map.key(
      key,
      entry => {
        val value = entry.value.as[YMap]

        value.key(
          "headers",
          entry => {
            val parameters: Seq[Parameter] =
              RamlParametersParser(entry.value.as[YMap], scheme.withHeader)
                .parse()
                .map(_.withBinding("header"))
            scheme.set(SecuritySchemeModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
          }
        )

        value.key(
          "queryParameters",
          entry => {
            val parameters: Seq[Parameter] =
              RamlParametersParser(entry.value.as[YMap], scheme.withQueryParameter)
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
            RamlTypeParser(queryEntry, (shape) => shape.adopted(scheme.id))
              .parse()
              .map(scheme.withQueryString)
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
                    responses += RamlResponseParser(entry, scheme.withResponse).parse()
                  })
                  scheme.set(SecuritySchemeModel.Responses,
                             AmfArray(responses, Annotations(entry.value)),
                             Annotations(entry))
                }
              )
          }
        )
      }
    )
  }
}

case class OasSecuritySchemeParser(ast: YPart, key: String, node: YNode, adopt: (SecurityScheme) => SecurityScheme)(
    implicit ctx: WebApiContext)
    extends SecuritySchemeParser {
  def parse(): SecurityScheme = {
    ctx.link(node) match {
      case Left(link) => parseReferenced(key, link, Annotations(node))
      case Right(value) =>
        val scheme = adopt(SecurityScheme(ast))

        val map = value.as[YMap]

        map.key(
          "type",
          entry => {
            val t: String = entry.value.as[YScalar].text match {
              case "oauth2" => "OAuth 2.0"
              case "basic"  => "Basic Authentication"
              case "apiKey" => "x-apiKey"
              case s        => s
            }

            scheme.set(SecuritySchemeModel.Type, AmfScalar(t, Annotations(entry.value)), Annotations(entry))
          }
        )

        map.key("x-displayName", entry => {
          val value = ValueNode(entry.value)
          scheme.set(SecuritySchemeModel.DisplayName, value.string(), Annotations(entry))
        })

        map.key("description", entry => {
          val value = ValueNode(entry.value)
          scheme.set(SecuritySchemeModel.Description, value.string(), Annotations(entry))
        })

        RamlDescribedByParser("x-describedBy", map, scheme).parse()

        OasSecuritySettingsParser(map, scheme)
          .parse()
          .foreach(scheme.set(SecuritySchemeModel.Settings, _, Annotations(ast)))

        AnnotationParser(() => scheme, map).parse()

        scheme
    }
  }

  case class OasSecuritySettingsParser(map: YMap, scheme: SecurityScheme) {
    def parse(): Option[Settings] = {
      val result = scheme.`type` match {
        case "OAuth 1.0" => Some(oauth1())
        case "OAuth 2.0" => Some(oauth2())
        case "x-apiKey"  => Some(apiKey())
        case _ =>
          map
            .key("x-settings")
            .map(entry => dynamicSettings(entry.value.as[YMap], scheme.withDefaultSettings()))
      }

      result.map(ss => {
        AnnotationParser(() => ss, map).parse()
        ss.add(Annotations(map))
      })
    }

    def dynamicSettings(xSettings: YMap, settings: Settings, properties: String*): Settings = {
      val entries = xSettings.entries.filterNot { entry =>
        val key: String = entry.key
        properties.contains(key) || WellKnownAnnotation.isOasAnnotation(key)
      }

      if (entries.nonEmpty) {
        val node = DataNodeParser(YNode(YMap(entries)), parent = Some(settings.id)).parse()
        settings.set(SettingsModel.AdditionalProperties, node)
      }

      AnnotationParser(() => scheme, xSettings).parse()

      settings
    }

    private def apiKey() = {
      val settings = scheme.withApiKeySettings()

      map.key("name", entry => {
        val value = ValueNode(entry.value)
        settings.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
      })

      map.key("in", entry => {
        val value = ValueNode(entry.value)
        settings.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
      })

      map.key(
        "x-settings",
        entry => dynamicSettings(entry.value.as[YMap], settings, "name", "in")
      )

      settings
    }

    private def oauth2() = {
      val settings = scheme.withOAuth2Settings()

      map.key("authorizationUrl", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth2SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
      })

      map.key("tokenUrl", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth2SettingsModel.AccessTokenUri, value.string(), Annotations(entry))
      })

      // TODO we should find similarity between raml authorizationGrants and this to map between values.
      map.key(
        "flow",
        entry => {
          val value = ValueNode(entry.value)
          settings.set(OAuth2SettingsModel.Flow, value.string(), Annotations(entry))
        }
      )

      map.key(
        "scopes",
        entry => {
          val scopeMap = entry.value.as[YMap]
          val scopes =
            scopeMap.entries.filterNot(entry => WellKnownAnnotation.isOasAnnotation(entry.key)).map(parseScope)

          AnnotationParser(() => scheme, scopeMap).parse()

          settings.setArray(OAuth2SettingsModel.Scopes, scopes, Annotations(entry))
        }
      )

      map.key(
        "x-settings",
        entry => {
          val xSettings = entry.value.as[YMap]

          xSettings.key(
            "authorizationGrants",
            entry => {
              val value = ArrayNode(entry.value)
              settings.set(OAuth2SettingsModel.AuthorizationGrants, value.strings(), Annotations(entry))
            }
          )

          dynamicSettings(xSettings, settings, "authorizationGrants")
        }
      )

      settings
    }

    private def parseScope(scopeEntry: YMapEntry) = {
      val name: String        = scopeEntry.key
      val description: String = scopeEntry.value

      Scope(scopeEntry)
        .set(ScopeModel.Name, AmfScalar(name), Annotations(scopeEntry.key))
        .set(ScopeModel.Description, AmfScalar(description), Annotations(scopeEntry.value))
    }

    private def oauth1() = {
      val settings = scheme.withOAuth1Settings()

      map.key(
        "x-settings",
        entry => {
          val xSettings = entry.value.as[YMap]

          xSettings.key("requestTokenUri", entry => {
            val value = ValueNode(entry.value)
            settings.set(OAuth1SettingsModel.RequestTokenUri, value.string(), Annotations(entry))
          })

          xSettings.key("authorizationUri", entry => {
            val value = ValueNode(entry.value)
            settings.set(OAuth1SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
          })

          xSettings.key("tokenCredentialsUri", entry => {
            val value = ValueNode(entry.value)
            settings.set(OAuth1SettingsModel.TokenCredentialsUri, value.string(), Annotations(entry))
          })

          xSettings.key("signatures", entry => {
            val value = ArrayNode(entry.value)
            settings.set(OAuth1SettingsModel.Signatures, value.strings(), Annotations(entry))
          })

          dynamicSettings(xSettings,
                          settings,
                          "requestTokenUri",
                          "authorizationUri",
                          "tokenCredentialsUri",
                          "signatures")
        }
      )

      settings
    }
  }

  def parseReferenced(name: String, parsedUrl: String, annotations: Annotations): SecurityScheme = {
    val scheme = ctx.declarations
      .findSecuritySchemeOrError(ast)(parsedUrl, SearchScope.Fragments)

    val copied: SecurityScheme = scheme.link(parsedUrl, annotations)
    copied.withName(name)
  }
}
