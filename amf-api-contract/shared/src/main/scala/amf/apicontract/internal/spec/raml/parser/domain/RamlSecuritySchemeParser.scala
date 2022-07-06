package amf.apicontract.internal.spec.raml.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{Parameter, Response}
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.spec.common.parser.{
  RamlParametersParser,
  SecuritySchemeParser,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  CrossSecurityWarningSpecification,
  InvalidSecuritySchemeDescribedByType,
  MissingSecuritySchemeErrorSpecification
}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.{ExternalFragmentRef, LexicalInformation}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.common.parser.AnnotationParser
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation.isRamlAnnotation
import amf.shapes.internal.spec.raml.parser.Raml10TypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.ExclusivePropertiesSpecification
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable

case class RamlSecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit
    ctx: RamlWebApiContext
) extends SecuritySchemeParser(part, adopt) {
  override def parse(): SecurityScheme = {
    val node           = getNode
    val (key, partKey) = getName

    ctx.link(node) match {
      case Left(link) => parseReferenced(key, partKey, link, Annotations(part), adopt)
      case Right(value) =>
        val scheme = adopt(SecurityScheme(part))

        val map = value.as[YMap]
        ctx.closedShape(scheme, map, "securitySchema")

        map.key("type", (SecuritySchemeModel.Type in scheme).allowingAnnotations)

        scheme.`type`.option() match {
          case Some("oauth2" | "basic" | "apiKey" | "http" | "openIdConnect") =>
            ctx.eh.warning(
              CrossSecurityWarningSpecification,
              scheme,
              Some(SecuritySchemeModel.Type.value.iri()),
              "OAS security scheme type detected in RAML spec",
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
                scheme,
                Some(SecuritySchemeModel.Type.value.iri()),
                "Security Scheme must have a mandatory value from 'OAuth 1.0', 'OAuth 2.0', 'Basic Authentication', 'Digest Authentication', 'Pass Through', x-<other>'",
                Some(LexicalInformation(map.range)),
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

        AnnotationParser(scheme, map, List(VocabularyMappings.securityScheme))(WebApiShapeParserContextAdapter(ctx))
          .parse()

        scheme
    }
  }

  def parseReferenced(
      name: String,
      partKey: Option[YNode],
      parsedUrl: String,
      annotations: Annotations,
      adopt: SecurityScheme => SecurityScheme
  ): SecurityScheme = {

    val scheme = ctx.declarations
      .findSecuritySchemeOrError(part)(parsedUrl, SearchScope.All)

    val copied: SecurityScheme = scheme.link(AmfScalar(parsedUrl), annotations, Annotations.synthesized())
    adopt(copied)
    copied.add(ExternalFragmentRef(parsedUrl))
    val keyAnn = partKey.map(k => Annotations(k)).getOrElse(Annotations())
    copied.setWithoutId(SecuritySchemeModel.Name, AmfScalar(name, keyAnn), keyAnn)
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
            ctx.closedShape(scheme, value, "describedBy")

            value.key(
              "headers",
              entry => {
                val parameters: Seq[Parameter] =
                  RamlParametersParser(
                    entry.value.as[YMap],
                    (p: Parameter) => Unit,
                    binding = "header"
                  ) // todo replace in separation
                    .parse()
                scheme.setWithoutId(
                  SecuritySchemeModel.Headers,
                  AmfArray(parameters, Annotations(entry.value)),
                  Annotations(entry)
                )
              }
            )

            if (value.key("queryParameters").isDefined && value.key("queryString").isDefined) {
              ctx.eh.violation(
                ExclusivePropertiesSpecification,
                scheme,
                s"Properties 'queryString' and 'queryParameters' are exclusive and cannot be declared together",
                value.location
              )
            }

            value.key(
              "queryParameters",
              entry => {
                val parameters: Seq[Parameter] =
                  RamlParametersParser(
                    entry.value.as[YMap],
                    (p: Parameter) => Unit,
                    binding = "query"
                  ) // todo replace in separation
                    .parse()
                scheme.setWithoutId(
                  SecuritySchemeModel.QueryParameters,
                  AmfArray(parameters, Annotations(entry.value)),
                  Annotations(entry)
                )
              }
            )

            value.key(
              "queryString",
              queryEntry => {
                Raml10TypeParser(queryEntry, shape => Unit)(WebApiShapeParserContextAdapter(ctx))
                  .parse()
                  .foreach(s => scheme.withQueryString(tracking(s, scheme)))
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

                    entries.foreach(entry => {
                      responses += ctx.factory
                        .responseParser(entry, (r: Response) => Unit, false)
                        .parse() // todo replace in separation
                    })
                }
                scheme.setWithoutId(
                  SecuritySchemeModel.Responses,
                  AmfArray(responses, Annotations(entry.value)),
                  Annotations(entry)
                )
              }
            )
            AnnotationParser(scheme, value)(WebApiShapeParserContextAdapter(ctx)).parse()
          case YType.Null =>
          case _ =>
            ctx.eh.violation(
              InvalidSecuritySchemeDescribedByType,
              scheme,
              s"Invalid 'describedBy' type, map expected",
              entry.value.location
            )
        }
      }
    )
  }
}
