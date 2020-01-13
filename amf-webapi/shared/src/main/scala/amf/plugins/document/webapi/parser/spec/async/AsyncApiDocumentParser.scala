package amf.plugins.document.webapi.parser.spec.async
import amf.core.Root
import amf.core.annotations.{DeclaredElement, SourceVendor}
import amf.core.model.document.Document
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, SyamlParsedDocument, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.document.webapi.parser.spec.domain.{AsyncServersParser, OasLikeInformationParser}
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.{EndPoint, WebApi}
import amf.validations.ParserSideValidations.InvalidIdentifier
import org.yaml.model.{YMap, YMapEntry, YType}

import scala.collection.mutable

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext) extends AsyncApiSpecParser {

  def parseDocument(): Document = parseDocument(Document())

  // TODO rewrite this reusing other parser when doing APIMF-1758
  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    parseDeclarations(map)

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document
      .withEncodes(api)
      .adopted(root.location)

    val declarable = ctx.declarations.declarables()
    if (declarable.nonEmpty) document.withDeclares(declarable)

    document
  }

  // TODO rewrite this reusing other parser when doing APIMF-1758
  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          ctx.declarations += ctx.factory
            .securitySchemeParser(
              entry,
              (scheme) => {
                val name = entry.key.as[String]
                scheme.set(SecuritySchemeModel.Name,
                           AmfScalar(name, Annotations(entry.key.value)),
                           Annotations(entry.key))
                scheme.adopted(parent)
              }
            )
            .parse()
            .add(DeclaredElement())
        }
      }
    )
  }

  // TODO rewrite this reusing other parser when doing APIMF-1758
  def parseDeclarations(map: YMap): Unit = {
    map.key("components").foreach { components =>
      val parent        = root.location + "#/declarations"
      val componentsMap = components.value.as[YMap]

      parseSecuritySchemeDeclarations(componentsMap, parent + "/securitySchemes")
    }
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
