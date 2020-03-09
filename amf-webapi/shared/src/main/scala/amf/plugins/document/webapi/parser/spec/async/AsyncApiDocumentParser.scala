package amf.plugins.document.webapi.parser.spec.async
import amf.core.Root
import amf.core.annotations.{DeclaredElement, SourceVendor}
import amf.core.model.document.Document
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, ScalarNode, SyamlParsedDocument, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.document.webapi.parser.spec.domain.binding.{
  AsyncChannelBindingsParser,
  AsyncMessageBindingsParser,
  AsyncOperationBindingsParser,
  AsyncServerBindingsParser
}
import amf.plugins.document.webapi.parser.spec.domain.{
  AsyncCorrelationIdParser,
  AsyncMessageParser,
  AsyncParametersParser,
  AsyncServersParser,
  OasLikeInformationParser
}
import amf.plugins.document.webapi.parser.spec.oas.OasLikeDeclarationsHelper
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.bindings.{
  ChannelBinding,
  ChannelBindings,
  MessageBinding,
  MessageBindings,
  OperationBinding,
  OperationBindings,
  ServerBinding,
  ServerBindings
}
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, WebApi}
import amf.validations.ParserSideValidations.InvalidIdentifier
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

import scala.collection.mutable

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext)
    extends AsyncApiSpecParser
    with OasLikeDeclarationsHelper {

  def parseDocument(): Document = parseDocument(Document())

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

  def parseWebApi(map: YMap): WebApi = {
    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)
    map.key("info", entry => OasLikeInformationParser(entry, api, ctx).parse())
    map.key("id", entry => IdentifierParser(entry, api, ctx).parse())
    map.key(
      "channels",
      entry => {
        val paths = entry.value.as[YMap]
        val endpoints = paths.entries.foldLeft(List[EndPoint]())((acc, curr) =>
          acc ++ ctx.factory.endPointParser(curr, api.withEndPoint, acc).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
      }
    )
    map.key(
      "externalDocs",
      entry => {
        api.set(WebApiModel.Documentations, OasLikeCreativeWorkParser(entry.value, api.id).parse())
      }
    )
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

  override protected val definitionsKey: String = "schemas"

  def parseDeclarations(map: YMap): Unit = {
    map.key("components").foreach { components =>
      val parent        = root.location + "#/declarations"
      val componentsMap = components.value.as[YMap]

      parseSecuritySchemeDeclarations(componentsMap, parent + "/securitySchemes")
      parseCorrelationIdDeclarations(componentsMap, parent + "/correlationIds")
      super.parseTypeDeclarations(componentsMap, parent + "/types")
      parseParameterDeclarations(componentsMap, parent + "/parameters")

      parseMessageBindingsDeclarations(componentsMap, parent + "/messageBindings")
      parseServerBindingsDeclarations(componentsMap, parent + "/serverBindings")
      parseOperationBindingsDeclarations(componentsMap, parent + "/operationBindings")
      parseChannelBindingsDeclarations(componentsMap, parent + "/channelBindings")

      // TODO operation & message traits (maintain the current order)

      parseMessageDeclarations(componentsMap, parent + "/messages")
    }
  }

  def parseMessageDeclarations(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "messages",
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          val message = AsyncMessageParser(parent, None).parseSingle(Left(entry))
          message.add(DeclaredElement())
          ctx.declarations += message
        }
      }
    )

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

  def parseParameterDeclarations(componentsMap: YMap, parent: String): Unit = {
    componentsMap.key(
      "parameters",
      paramsMap => {
        val parameters: Seq[Parameter] = AsyncParametersParser(parent, paramsMap.value.as[YMap]).parse()
        parameters map { param =>
          param.add(DeclaredElement())
          ctx.declarations += param
        }
      }
    )
  }

  def parseCorrelationIdDeclarations(componentsMap: YMap, parent: String): Unit = {
    componentsMap.key(
      "correlationIds",
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          val correlationId = AsyncCorrelationIdParser(Left(entry), parent).parse()
          ctx.declarations += correlationId.add(DeclaredElement())
        }
      }
    )
  }

  def parseMessageBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[MessageBindings](
      "messageBindings",
      componentsMap,
      (entry) => {
        AsyncMessageBindingsParser.parse(Left(entry), parent)
      }
    )
  }

  def parseServerBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ServerBindings](
      "serverBindings",
      componentsMap,
      (entry) => {
        AsyncServerBindingsParser.parse(Left(entry), parent)
      }
    )
  }

  def parseOperationBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[OperationBindings](
      "operationBindings",
      componentsMap,
      (entry) => {
        AsyncOperationBindingsParser.parse(Left(entry), parent)
      }
    )
  }

  def parseChannelBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ChannelBindings](
      "channelBindings",
      componentsMap,
      (entry) => {
        AsyncChannelBindingsParser.parse(Left(entry), parent)
      }
    )
  }

  def parseBindingsDeclarations[T <: DomainElement](keyword: String,
                                                    componentsMap: YMap,
                                                    parse: (YMapEntry) => T): Unit = {
    componentsMap.key(
      keyword,
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          val bindings: T = parse(entry)
          bindings.add(DeclaredElement())
          ctx.declarations += bindings
        }
      }
    )
  }

}

case class IdentifierParser(entry: YMapEntry, webApi: WebApi, override implicit val ctx: AsyncWebApiContext)
    extends WebApiBaseSpecParser {
  def parse(): Unit = {
    entry.value.tagType match {
      case YType.Str =>
        val id = entry.value.toString
        webApi.set(WebApiModel.Identifier, AmfScalar(id), Annotations(entry))
      case _ =>
        ctx.eh.violation(InvalidIdentifier, webApi.id, "'id' must be a string", entry.location)
    }
  }
}

abstract class AsyncApiSpecParser(implicit ctx: AsyncWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {}
