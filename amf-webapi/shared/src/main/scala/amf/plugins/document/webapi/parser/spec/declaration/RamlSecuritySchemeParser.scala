package amf.plugins.document.webapi.parser.spec.declaration
import amf.core.annotations.LexicalInformation
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, Range, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import amf.plugins.document.webapi.parser.spec.domain.{RamlParametersParser, RamlSecuritySettingsParser}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{Parameter, Response}
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YPart, YScalar, YType}

import scala.collection.mutable

case class RamlSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(
    implicit ctx: RamlWebApiContext)
    extends SecuritySchemeParser(part, adopt) {
  override def parse(): SecurityScheme = {
    val node = getNode
    val key  = getName

    ctx.link(node) match {
      case Left(link) => parseReferenced(key, link, Annotations(node), adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(part))

        val map = value.as[YMap]
        ctx.closedShape(scheme.id, map, "securitySchema")

        map.key("type", (SecuritySchemeModel.Type in scheme).allowingAnnotations)

        scheme.`type`.option() match {
          case Some("oauth2" | "basic" | "apiKey") =>
            ctx.eh.warning(
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
              ctx.eh.violation(
                MissingSecuritySchemeErrorSpecification,
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
                      adopt: SecurityScheme => SecurityScheme): SecurityScheme = {

    val scheme = ctx.declarations
      .findSecuritySchemeOrError(part)(parsedUrl, SearchScope.All)

    val copied: SecurityScheme = scheme.link(parsedUrl, annotations)
    adopt(copied)
    copied.withName(name)
  }
}

case class RamlDescribedByParser(key: String, map: YMap, scheme: SecurityScheme)(implicit ctx: RamlWebApiContext) {
  def parse(): Unit = {
    map.key(
      key,
      entry => {
        entry.value.tagType match {
          case YType.Map =>
            val value = entry.value.as[YMap]
            ctx.closedShape(scheme.id, value, "describedBy")

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
              ctx.eh.violation(
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
                      ctx.eh.violation(DuplicatedOperationStatusCodeSpecification,
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
          case YType.Null =>
          case _ =>
            ctx.eh.violation(InvalidSecuritySchemeDescribedByType,
                             scheme.id,
                             s"Invalid 'describedBy' type, map expected",
                             entry.value)
        }
      }
    )
  }
}
