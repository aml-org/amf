package amf.plugins.document.webapi.parser.spec.async
import amf.core.Root
import amf.core.annotations.{DeclaredElement, SourceVendor}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.Document
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, ScalarNode, SyamlParsedDocument, YMapOps}
import amf.plugins.document.webapi.annotations.{DeclarationKey, DeclarationKeys}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.async.parser._
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.declaration.{OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.domain.binding.{
  AsyncChannelBindingsParser,
  AsyncMessageBindingsParser,
  AsyncOperationBindingsParser,
  AsyncServerBindingsParser
}
import amf.plugins.document.webapi.parser.spec.oas.OasLikeDeclarationsHelper
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.metamodel.bindings.{
  ChannelBindingsModel,
  MessageBindingsModel,
  OperationBindingsModel,
  ServerBindingsModel
}
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, WebApi}
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YMapEntry, YType}

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext)
    extends AsyncApiSpecParser
    with OasLikeDeclarationsHelper {

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    parseDeclarations(map)
    val declarationKeys = ctx.getDeclarationKeys
    if (declarationKeys.nonEmpty) document.add(DeclarationKeys(declarationKeys))

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document
      .withEncodes(api)
      .adopted(root.location)

    val declarable = ctx.declarations.declarables()
    if (declarable.nonEmpty) document.withDeclares(declarable)

    document
  }

  def parseWebApi(map: YMap): WebApi = {
    YamlTagValidator.validate(root)
    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)
    map.key("info", entry => OasLikeInformationParser(entry, api, ctx).parse())
    map.key("id", entry => IdentifierParser(entry, api, ctx).parse())
    map.key("channels") match {
      case Some(entry) => parseChannels(entry, api)
      case None        => ctx.eh.violation(MandatoryChannelsProperty, api.id, "'channels' is mandatory in async spec")
    }
    map.key(
      "externalDocs",
      entry => {
        api.setArray(WebApiModel.Documentations, Seq(OasLikeCreativeWorkParser(entry.value, api.id).parse()))
      }
    )
    map.key(
      "servers",
      entry => {
        val servers = AsyncServersParser(entry.value.as[YMap], api).parse()
        api.set(WebApiModel.Servers, AmfArray(servers, Annotations(entry.value)), Annotations(entry))
      }
    )
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

  private def parseChannels(entry: YMapEntry, api: WebApi): Unit = {
    val paths = entry.value.as[YMap]
    val endpoints = paths.entries.foldLeft(List[EndPoint]())((acc, curr) =>
      acc ++ ctx.factory.endPointParser(curr, api.withEndPoint, acc).parse())
    api.set(WebApiModel.EndPoints, AmfArray(endpoints, Annotations(entry.value)), Annotations(entry))
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
      parseOperationTraits(componentsMap, parent + "/operationTraits")
      parseMessageTraits(componentsMap, parent + "/messageTraits")

      parseMessageDeclarations(componentsMap, parent + "/messages")
    }
  }

  def parseMessageDeclarations(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "messages",
      e => {
        ctx.addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val message = AsyncMessageParser(YMapEntryLike(entry), parent, None).parse()
          message.add(DeclaredElement())
          ctx.declarations += message
        }
      }
    )

  def parseOperationTraits(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "operationTraits",
      entry => {
        ctx.addDeclarationKey(DeclarationKey(entry, isAbstract = true))
        entry.value.as[YMap].entries.foreach { entry =>
          val produceOperation = (name: String) => Operation().withName(name).withMethod(name).adopted(parent)
          val operation        = AsyncOperationParser(entry, produceOperation, isTrait = true).parse()
          operation.add(DeclaredElement())
          ctx.declarations += operation
        }
      }
    )

  def parseMessageTraits(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "messageTraits",
      entry => {
        ctx.addDeclarationKey(DeclarationKey(entry, isAbstract = true))
        entry.value.as[YMap].entries.foreach { entry =>
          val message = AsyncMessageParser(YMapEntryLike(entry), parent, None, isTrait = true).parse()
          message.add(DeclaredElement())
          ctx.declarations += message
        }
      }
    )

  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        ctx.addDeclarationKey(DeclarationKey(e))
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
        ctx.addDeclarationKey(DeclarationKey(paramsMap))
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
        ctx.addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val correlationId = AsyncCorrelationIdParser(YMapEntryLike(entry), parent).parse()
          ctx.declarations += correlationId.add(DeclaredElement())
        }
      }
    )
  }

  def parseMessageBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[MessageBindings](
      "messageBindings",
      componentsMap,
      entry => {
        AsyncMessageBindingsParser(YMapEntryLike(entry), parent).parse()
      },
      MessageBindingsModel
    )
  }

  def parseServerBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ServerBindings](
      "serverBindings",
      componentsMap,
      entry => {
        AsyncServerBindingsParser(YMapEntryLike(entry), parent).parse()
      },
      ServerBindingsModel
    )
  }

  def parseOperationBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[OperationBindings](
      "operationBindings",
      componentsMap,
      entry => {
        AsyncOperationBindingsParser(YMapEntryLike(entry), parent).parse()
      },
      OperationBindingsModel
    )
  }

  def parseChannelBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ChannelBindings](
      "channelBindings",
      componentsMap,
      entry => {
        AsyncChannelBindingsParser(YMapEntryLike(entry), parent).parse()
      },
      ChannelBindingsModel
    )
  }

  def parseBindingsDeclarations[T <: DomainElement](keyword: String,
                                                    componentsMap: YMap,
                                                    parse: YMapEntry => T,
                                                    model: DomainElementModel): Unit = {
    componentsMap.key(
      keyword,
      e => {
        ctx.addDeclarationKey(DeclarationKey(e))
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
        val id = entry.value.as[String]
        webApi.set(WebApiModel.Identifier, AmfScalar(id), Annotations(entry))
      case _ =>
        ctx.eh.violation(InvalidIdentifier, webApi.id, "'id' must be a string", entry.location)
    }
  }
}

abstract class AsyncApiSpecParser(implicit ctx: AsyncWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {}
