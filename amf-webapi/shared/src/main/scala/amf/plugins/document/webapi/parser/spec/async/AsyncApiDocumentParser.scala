package amf.plugins.document.webapi.parser.spec.async
import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Document
import amf.core.parser.{SyamlParsedDocument, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.{YType, YMap, YMapEntry}
import amf.plugins.document.webapi.parser.spec.domain.OasLikeInformationParser
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.validations.ParserSideValidations.InvalidIdentifier

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext) extends AsyncApiSpecParser {

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document
      .withEncodes(api)
      .adopted(root.location)

    document
  }

  def parseWebApi(map: YMap): WebApi = {
    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)
    map.key("info", entry => OasLikeInformationParser(entry, api, ctx))
    map.key("id", entry => IdentifierParser(entry, api, ctx))
//    map.key("channels", entry => ChannelsParser(entry, api, ctx))
    map.key("externalDocs", WebApiModel.Documentations in api using (OasLikeCreativeWorkParser.parse(_, api.id)))
    map.key("tags", entry => OasLikeTagsParser(entry, api).parse())
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
        ctx.violation(InvalidIdentifier, webApi.id, "'id' must be a string", entry.location)
    }
  }
}

abstract class AsyncApiSpecParser(implicit ctx: AsyncWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {}
