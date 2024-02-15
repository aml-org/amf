package amf.apicontract.internal.spec.async.parser.document

import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.declarations.AsyncDeclarationParser
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser.document.OasLikeDeclarationsHelper
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeInformationParser, OasLikeTagsParser}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  InvalidIdentifier,
  MandatoryChannelsProperty
}
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser}
import org.yaml.model.{YMap, YMapEntry, YType}

abstract class AsyncApiDocumentParser(root: Root, spec: Spec, declarationParser: AsyncDeclarationParser)(implicit
    val ctx: AsyncWebApiContext
) extends AsyncApiSpecParser
    with OasLikeDeclarationsHelper {

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.withLocation(root.location).withProcessingData(APIContractProcessingData().withSourceSpec(spec))

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    ctx.setJsonSchemaAST(map)

    val references = AsyncReferencesParser(root.references).parse()
    parseDeclarations(map, document)

    val api = parseApi(map)
    document
      .setWithoutId(DocumentModel.Encodes, api, Annotations.inferred())

    if (references.nonEmpty) document.withReferences(references.baseUnitReferences())

    ctx.futureDeclarations.resolve()
    document
  }

  private def parseDeclarations(map: YMap, document: Document) = {
    map.key("components").foreach { components =>
      val parent        = root.location + "#/declarations"
      val componentsMap = components.value.as[YMap]
      declarationParser.parseDeclarations(componentsMap, parent, document)
      ctx.closedShape(document, componentsMap, "components")
//      addDeclarationsToModel(document) // TODO: had to move this to each declaration parser as HF for release RCs
      validateNames()
    }
  }

  protected def parseApi(map: YMap): AsyncApi = {
    YamlTagValidator.validate(root)
    val api = AsyncApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node)
    map.key("info", entry => OasLikeInformationParser(entry, api, ctx).parse())
    map.key("id", entry => IdentifierParser(entry, api, ctx).parse())
    map.key("channels") match {
      case Some(entry) => parseChannels(entry, api)
      case None        => ctx.eh.violation(MandatoryChannelsProperty, api, "'channels' is mandatory in async spec")
    }
    map.key(
      "externalDocs",
      entry => {
        api.setWithoutId(
          WebApiModel.Documentations,
          AmfArray(
            Seq(OasLikeCreativeWorkParser(entry.value, api.id).parse()),
            Annotations(entry.value)
          ),
          Annotations(entry)
        )
      }
    )
    map.key(
      "servers",
      entry => {
        val servers = ctx.factory.serversParser(entry.value.as[YMap], api).parse()
        api.setWithoutId(WebApiModel.Servers, AmfArray(servers, Annotations(entry.value)), Annotations(entry))
      }
    )
    map.key(
      "tags",
      entry => {
        val tags = OasLikeTagsParser(api.id, entry).parse()
        api.setWithoutId(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )
    map.key(
      "defaultContentType",
      entry => {
        val annotations = Annotations(entry)
        val contentType = ScalarNode(entry.value).string()
        val value       = AmfArray(Seq(contentType))
        api.setWithoutId(WebApiModel.ContentType, value, annotations)
        api.setWithoutId(WebApiModel.Accepts, value, annotations)
      }
    )

    AnnotationParser(api, map).parse()
    AnnotationParser(api, map).parseOrphanNode("channels")

    ctx.closedShape(api, map, "webApi")
    api
  }

  private def parseChannels(entry: YMapEntry, api: AsyncApi): Unit = {
    val paths = entry.value.as[YMap]
    val endpoints = paths.entries.foldLeft(List[EndPoint]())((acc, curr) =>
      acc ++ ctx.factory.endPointParser(curr, api.id, acc).parse()
    )
    api.setWithoutId(WebApiModel.EndPoints, AmfArray(endpoints, Annotations(entry.value)), Annotations(entry))
  }

  override protected val definitionsKey: String = "schemas"
}

case class IdentifierParser(entry: YMapEntry, webApi: AsyncApi, override implicit val ctx: AsyncWebApiContext)
    extends WebApiBaseSpecParser {
  def parse(): Unit = {
    entry.value.tagType match {
      case YType.Str =>
        val id = entry.value.as[String]
        webApi.setWithoutId(WebApiModel.Identifier, AmfScalar(id, Annotations(entry.value)), Annotations(entry))
      case _ =>
        ctx.eh.violation(InvalidIdentifier, webApi, "'id' must be a string", entry.location)
    }
  }
}

abstract class AsyncApiSpecParser(implicit ctx: AsyncWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {}
