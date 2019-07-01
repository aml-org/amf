package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.LexicalInformation
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.remote.{Oas, Raml}
import amf.core.utils.Strings
import amf.plugins.document.webapi.contexts.{RamlWebApiContext, WebApiContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.{isOasAnnotation, isRamlAnnotation}
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.models.security.{Scope, SecurityScheme, Settings}
import amf.plugins.domain.webapi.models.{Parameter, Response}
import amf.plugins.features.validation.ParserSideValidations
import amf.plugins.features.validation.ParserSideValidations.{
  CrossSecurityWarningSpecification,
  DuplicatedOperationStatusCodeSpecification,
  ExclusivePropertiesSpecification
}
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
      case other =>
        ctx.violation(ParserSideValidations.UnexpectedVendor,
                      "",
                      s"Unsupported vendor $other in security scheme parsers",
                      entry)
        RamlSecuritySchemeParser(entry, entry.key.as[YScalar].text, entry.value, adopt)(toRaml(ctx)) // use raml as default?
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

        scheme.`type`.option() match {
          case Some("oauth2" | "basic" | "apiKey") =>
            ctx.warning(
              CrossSecurityWarningSpecification,
              scheme.id,
              Some(SecuritySchemeModel.Type.value.iri()),
              "OAS 2.0 security scheme type detected in RAML 1.0 spec",
              scheme.`type`.annotations().find(classOf[LexicalInformation]),
              Some(ctx.rootContextDocument)
            )
          case _ => // this will be checked during validation
        }
        map.key(
          "type",
          value => {
            // we need to check this because of the problem parsing nulls like empty strings of value null
            if (value.value.tagType == YType.Null && scheme.`type`.option().contains("")) {
              ctx.violation(
                ParserSideValidations.MissingSecuritySchemeErrorSpecification,
                scheme.id,
                Some(SecuritySchemeModel.Type.value.iri()),
                "Security Scheme must have a mandatory value from 'OAuth 1.0', 'OAuth 2.0', 'Basic Authentication', 'Digest Authentication', 'Pass Through', x-<other>'",
                Some(LexicalInformation(Range(map.range))),
                Some(ctx.rootContextDocument)
              )
            }
          }
        )
        scheme.normalizeType() // normalize the common type
        map.key("displayName", (SecuritySchemeModel.DisplayName in scheme).allowingAnnotations)
        map.key("description", (SecuritySchemeModel.Description in scheme).allowingAnnotations)

        RamlDescribedByParser("describedBy", map, scheme).parse()

        map.key("settings", SecuritySchemeModel.Settings in scheme using RamlSecuritySettingsParser.parse(scheme))

        AnnotationParser(scheme, map, List(VocabularyMappings.securityScheme)).parse()

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

            if (value.key("queryParameters").isDefined && value.key("queryString").isDefined) {
              ctx.violation(
                ExclusivePropertiesSpecification,
                scheme.id,
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together",
                value
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
                val responses = mutable.ListBuffer[Response]()
                entry.value.tagType match {
                  case YType.Null => // ignore
                  case _ =>
                    val entries = entry.value
                      .as[YMap]
                      .entries
                      .filter(y => !isRamlAnnotation(y.key.as[YScalar].text))

                    val keys   = entries.map(_.key.as[YScalar].text)
                    val keySet = keys.toSet
                    if (keys.size > keySet.size) {
                      ctx.violation(DuplicatedOperationStatusCodeSpecification,
                                    scheme.id,
                                    None,
                                    "RAML Responses must not have duplicated status codes",
                                    entry.value)
                    }

                    entries.foreach(entry => {
                      responses += ctx.factory
                        .responseParser(entry, (r: Response) => r.adopted(scheme.id), false)
                        .parse() // todo replace in separation
                    })
                }
                scheme.set(SecuritySchemeModel.Responses,
                           AmfArray(responses, Annotations(entry.value)),
                           Annotations(entry))
              }
            )

            AnnotationParser(scheme, value).parse()

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

        // 3 stages
        // 2 pipes

        map.key("type", SecuritySchemeModel.Type in scheme)

        scheme.`type`.option() match {
          case Some(s) if s.startsWith("x-") =>
            ctx.warning(
              CrossSecurityWarningSpecification,
              scheme.id,
              Some(SecuritySchemeModel.Type.value.iri()),
              s"RAML 1.0 extension security scheme type '$s' detected in OAS 2.0 spec",
              scheme.`type`.annotations().find(classOf[LexicalInformation]),
              Some(ctx.rootContextDocument)
            )
          case Some("OAuth 1.0" | "OAuth 2.0" | "Basic Authentication" | "Digest Authentication" | "Pass Through") =>
            ctx.warning(
              CrossSecurityWarningSpecification,
              scheme.id,
              Some(SecuritySchemeModel.Type.value.iri()),
              s"RAML 1.0 security scheme type detected in OAS 2.0 spec",
              scheme.`type`.annotations().find(classOf[LexicalInformation]),
              Some(ctx.rootContextDocument)
            )
          case _ =>
        }

        map.key(
          "type",
          value => {
            // we need to check this because of the problem parsing nulls like empty strings of value null
            if (value.value.tagType == YType.Null && scheme.`type`.option().contains("")) {
              ctx.violation(
                ParserSideValidations.MissingSecuritySchemeErrorSpecification,
                scheme.id,
                Some(SecuritySchemeModel.Type.value.iri()),
                "Security Scheme must have a mandatory value from 'oauth2', 'basic' or 'apiKey'",
                Some(LexicalInformation(Range(map.range))),
                Some(ctx.rootContextDocument)
              )
            }
          }
        )

        scheme.normalizeType() // normalize the common type

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
        case "OAuth 1.0" => Some(oauth1())
        case "OAuth 2.0" => Some(oauth2())
        case "Api Key"   => Some(apiKey())
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
