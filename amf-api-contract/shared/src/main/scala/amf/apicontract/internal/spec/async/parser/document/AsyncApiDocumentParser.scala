package amf.apicontract.internal.spec.async.parser.document

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.client.scala.model.domain.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.bindings.{ChannelBindingsModel, MessageBindingsModel, OperationBindingsModel, ServerBindingsModel}
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.spec.async.parser.bindings.{AsyncChannelBindingsParser, AsyncMessageBindingsParser, AsyncOperationBindingsParser, AsyncServerBindingsParser}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain._
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser.document.OasLikeDeclarationsHelper
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeInformationParser, OasLikeTagsParser}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{InvalidIdentifier, MandatoryChannelsProperty}
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar, DomainElement}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.{DeclaredElement, SourceSpec}
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry, YType}

abstract class AsyncApiDocumentParser(root: Root)(implicit val ctx: AsyncWebApiContext)
    extends AsyncApiSpecParser
    with OasLikeDeclarationsHelper {

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.withLocation(root.location).withProcessingData(APIContractProcessingData().withSourceSpec(Spec.ASYNC20))

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    ctx.setJsonSchemaAST(map)

    val references = AsyncReferencesParser(root.references).parse()
    parseDeclarations(map, document)

    val api = parseApi(map).add(SourceSpec(ctx.spec))
    document
      .setWithoutId(DocumentModel.Encodes, api, Annotations.inferred())

    addDeclarationsToModel(document)
    if (references.nonEmpty) document.withReferences(references.baseUnitReferences())

    ctx.futureDeclarations.resolve()
    document
  }

  def parseApi(map: YMap): AsyncApi = {
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
          AmfArray(Seq(OasLikeCreativeWorkParser(entry.value, api.id)(WebApiShapeParserContextAdapter(ctx)).parse()),
                   Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )
    map.key(
      "servers",
      entry => {
        val servers = AsyncServersParser(entry.value.as[YMap], api).parse()
        api.setWithoutId(WebApiModel.Servers, AmfArray(servers, Annotations(entry.value)), Annotations(entry))
      }
    )
    map.key("tags", entry => {
      val tags = OasLikeTagsParser(api.id, entry).parse()
      api.setWithoutId(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
    })
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

    AnnotationParser(api, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    AnnotationParser(api, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("channels")

    ctx.closedShape(api, map, "webApi")
    api
  }

  private def parseChannels(entry: YMapEntry, api: AsyncApi): Unit = {
    val paths = entry.value.as[YMap]
    val endpoints = paths.entries.foldLeft(List[EndPoint]())((acc, curr) =>
      acc ++ ctx.factory.endPointParser(curr, api.id, acc).parse())
    api.setWithoutId(WebApiModel.EndPoints, AmfArray(endpoints, Annotations(entry.value)), Annotations(entry))
  }

  override protected val definitionsKey: String = "schemas"

  def parseDeclarations(map: YMap, parentObj: AmfObject): Unit = {
    map.key("components").foreach { components =>
      val parent        = root.location + "#/declarations"
      val componentsMap = components.value.as[YMap]

      parseSecuritySchemeDeclarations(componentsMap, parent + "/securitySchemes")
      parseCorrelationIdDeclarations(componentsMap, parent + "/correlationIds")
      super.parseTypeDeclarations(componentsMap, parent + "/types", Some(this))
      parseParameterDeclarations(componentsMap, parent + "/parameters")

      parseMessageBindingsDeclarations(componentsMap, parent + "/messageBindings")
      parseServerBindingsDeclarations(componentsMap, parent + "/serverBindings")
      parseOperationBindingsDeclarations(componentsMap, parent + "/operationBindings")
      parseChannelBindingsDeclarations(componentsMap, parent + "/channelBindings")
      parseOperationTraits(componentsMap, parent + "/operationTraits")
      parseMessageTraits(componentsMap, parent + "/messageTraits")

      parseMessageDeclarations(componentsMap, parent + "/messages")

      ctx.closedShape(parentObj, componentsMap, "components")
      validateNames()
    }
  }

  def parseMessageDeclarations(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "messages",
      e => {
        addDeclarationKey(DeclarationKey(e))
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
        addDeclarationKey(DeclarationKey(entry, isAbstract = true))
        entry.value.as[YMap].entries.foreach { entry =>
          val adopt     = (o: Operation) => o
          val operation = AsyncOperationParser(entry, adopt, isTrait = true).parse()
          operation.add(DeclaredElement())
          ctx.declarations += operation
        }
      }
    )

  def parseMessageTraits(componentsMap: YMap, parent: String): Unit =
    componentsMap.key(
      "messageTraits",
      entry => {
        addDeclarationKey(DeclarationKey(entry, isAbstract = true))
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
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          ctx.declarations += ctx.factory
            .securitySchemeParser(
              entry,
              (scheme) => {
                val name = entry.key.as[String]
                scheme.setWithoutId(SecuritySchemeModel.Name,
                           AmfScalar(name, Annotations(entry.key.value)),
                           Annotations(entry.key))
                scheme
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
        addDeclarationKey(DeclarationKey(paramsMap))
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
        addDeclarationKey(DeclarationKey(e))
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
        AsyncMessageBindingsParser(YMapEntryLike(entry)).parse()
      },
      MessageBindingsModel
    )
  }

  def parseServerBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ServerBindings](
      "serverBindings",
      componentsMap,
      entry => {
        AsyncServerBindingsParser(YMapEntryLike(entry)).parse()
      },
      ServerBindingsModel
    )
  }

  def parseOperationBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[OperationBindings](
      "operationBindings",
      componentsMap,
      entry => {
        AsyncOperationBindingsParser(YMapEntryLike(entry)).parse()
      },
      OperationBindingsModel
    )
  }

  def parseChannelBindingsDeclarations(componentsMap: YMap, parent: String): Unit = {
    parseBindingsDeclarations[ChannelBindings](
      "channelBindings",
      componentsMap,
      entry => {
        AsyncChannelBindingsParser(YMapEntryLike(entry)).parse()
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
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val bindings: T = parse(entry)
          bindings.add(DeclaredElement())
          ctx.declarations += bindings
        }
      }
    )
  }

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
