package amf.plugins.document.webapi.parser.spec.async
import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Document
import amf.core.model.domain.{DomainElement, AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, SyamlParsedDocument, YMapOps}
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.domain.webapi.models.{EndPoint, WebApi}
import org.yaml.model.{YMap, YMapEntry, YType}
import amf.plugins.document.webapi.parser.spec.domain.{AsyncServersParser, OasLikeInformationParser}
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.security.{ParametrizedSecuritySchemeModel, SecuritySchemeModel}
import amf.validations.ParserSideValidations
import amf.validations.ParserSideValidations.{InvalidIdentifier, InvalidComponents}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext) extends AsyncApiSpecParser {

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    val api          = parseWebApi(map).add(SourceVendor(ctx.vendor))
    val declarations = parseDeclarations(map)
    document
      .withEncodes(api)
      .withDeclares(declarations)
      .adopted(root.location)

    document
  }

  def parseDeclarations(map: YMap): Seq[DomainElement] = {
    val parent    = root.location + "#/declarations"
    val collector = ListBuffer.empty[DomainElement]
    map.key(
      "components",
      entry => {
        entry.value.tagType match {
          case YType.Map =>
            val componentsMap = entry.value.as[YMap]
            componentsMap.key(
              "securitySchemes",
              entry => {
                entry.value.as[YMap].entries.foreach { securitySchemeEntry =>
                  val securityScheme = ctx.factory
                    .securitySchemeParser(
                      securitySchemeEntry,
                      (scheme) => {
                        val name = securitySchemeEntry.key.as[String]
                        scheme.set(SecuritySchemeModel.Name,
                                   AmfScalar(name, Annotations(securitySchemeEntry.key.value)),
                                   Annotations(securitySchemeEntry.key))
                        scheme.adopted(parent + "/securitySchemes")
                      }
                    )
                    .parse()
                  collector += securityScheme
                }
              }
            )
          case _ =>
            ctx.violation(InvalidComponents, root.location, "Components facet must be a map")
        }
      }
    )
    collector
  }

  def parseWebApi(map: YMap): WebApi = {
    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)
    map.key("info", entry => OasLikeInformationParser(entry, api, ctx).parse())
    map.key("id", entry => IdentifierParser(entry, api, ctx).parse())
    map.key(
      "channels",
      entry => {
        val paths     = entry.value.as[YMap]
        val endpoints = mutable.ListBuffer[EndPoint]()
        paths.entries.foreach(ctx.factory.endPointParser(_, api.withEndPoint, endpoints).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
      }
    )
    map.key("externalDocs", WebApiModel.Documentations in api using (OasLikeCreativeWorkParser.parse(_, api.id)))
    map.key("servers", entry => {
      val servers = AsyncServersParser(entry.value.as[YMap], api).parse()
      api.withServers(servers)
    })
    map.key("tags", entry => {
      val tags = OasLikeTagsParser(api.id, entry).parse()
      api.set(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
    })
    map.key(
      "defaultContentType",
      entry => {
        val annotations = Annotations(entry)
        val contentType = ScalarNode(entry.value).string()
        val value       = AmfArray(Seq(contentType))
        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
      }
    )

    AnnotationParser(api, map).parse()
    AnnotationParser(api, map).parseOrphanNode("channels")

    ctx.closedShape(api.id, map, "webApi")
    api
  }

}

case class IdentifierParser(entry: YMapEntry, webApi: WebApi, override implicit val ctx: AsyncWebApiContext)
    extends WebApiBaseSpecParser {
  def parse(): Unit = {
    entry.value.tagType match {
      case YType.Str =>
        val id = entry.value.toString
        webApi.withIdentifier(id)
      case _ =>
        ctx.eh.violation(InvalidIdentifier, webApi.id, "'id' must be a string", entry.location)
    }
  }
}

abstract class AsyncApiSpecParser(implicit ctx: AsyncWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {}
