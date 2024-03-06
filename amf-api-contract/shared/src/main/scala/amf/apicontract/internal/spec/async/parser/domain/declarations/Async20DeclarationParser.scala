package amf.apicontract.internal.spec.async.parser.domain.declarations

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.client.scala.model.domain.bindings.{
  ChannelBindings,
  MessageBindings,
  OperationBindings,
  ServerBindings
}
import amf.apicontract.client.scala.model.domain.{Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.bindings.{
  ChannelBindingsModel,
  MessageBindingsModel,
  OperationBindingsModel,
  ServerBindingsModel
}
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.spec.async.parser.bindings.{
  AsyncChannelBindingsParser,
  AsyncMessageBindingsParser,
  AsyncOperationBindingsParser,
  AsyncServerBindingsParser
}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.{
  AsyncCorrelationIdParser,
  AsyncOperationParser,
  AsyncParametersParser
}
import amf.apicontract.internal.spec.oas.parser.document.OasLikeDeclarationsHelper
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

case class Async20DeclarationParser() extends AsyncDeclarationParser with OasLikeDeclarationsHelper {

  protected val definitionsKey = "schemas"

  override def parseDeclarations(map: YMap, parent: String, document: Document)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
    parseCorrelationIdDeclarations(map, parent + "/correlationIds")
    super.parseTypeDeclarations(map, Some(this))
    parseParameterDeclarations(map, parent + "/parameters")

    parseMessageBindingsDeclarations(map, parent + "/messageBindings")
    parseServerBindingsDeclarations(map, parent + "/serverBindings")
    parseOperationBindingsDeclarations(map, parent + "/operationBindings")
    parseChannelBindingsDeclarations(map, parent + "/channelBindings")
    parseOperationTraits(map, parent + "/operationTraits")
    parseMessageTraits(map, parent + "/messageTraits")

    parseMessageDeclarations(map, parent + "/messages")

    super.parseDeclarations(map, parent, document)
  }

  private def parseMessageDeclarations(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
    componentsMap.key(
      "messages",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          val message = ctx.factory.messageParser(YMapEntryLike(entry), parent, None).parse()
          message.add(DeclaredElement())
          ctx.declarations += message
        }
      }
    )

  private def parseOperationTraits(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
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

  private def parseMessageTraits(componentsMap: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit =
    componentsMap.key(
      "messageTraits",
      entry => {
        addDeclarationKey(DeclarationKey(entry, isAbstract = true))
        entry.value.as[YMap].entries.foreach { entry =>
          val message = ctx.factory.messageParser(YMapEntryLike(entry), parent, None, isTrait = true).parse()
          message.add(DeclaredElement())
          ctx.declarations += message
        }
      }
    )

  private def parseSecuritySchemeDeclarations(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "securitySchemes",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.as[YMap].entries.foreach { entry =>
          ctx.declarations += ctx.factory
            .securitySchemeParser(
              YMapEntryLike(entry),
              (scheme) => {
                val name = entry.key.as[String]
                scheme.setWithoutId(
                  SecuritySchemeModel.Name,
                  AmfScalar(name, Annotations(entry.key.value)),
                  Annotations(entry.key)
                )
                scheme
              }
            )
            .parse()
            .add(DeclaredElement())
        }
      }
    )
  }

  private def parseParameterDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
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

  private def parseCorrelationIdDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
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

  private def parseMessageBindingsDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseBindingsDeclarations[MessageBindings](
      "messageBindings",
      componentsMap,
      entry => {
        AsyncMessageBindingsParser(YMapEntryLike(entry)).parse()
      },
      MessageBindingsModel
    )
  }

  private def parseServerBindingsDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseBindingsDeclarations[ServerBindings](
      "serverBindings",
      componentsMap,
      entry => {
        AsyncServerBindingsParser(YMapEntryLike(entry)).parse()
      },
      ServerBindingsModel
    )
  }

  private def parseOperationBindingsDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseBindingsDeclarations[OperationBindings](
      "operationBindings",
      componentsMap,
      entry => {
        AsyncOperationBindingsParser(YMapEntryLike(entry)).parse()
      },
      OperationBindingsModel
    )
  }

  private def parseChannelBindingsDeclarations(componentsMap: YMap, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    parseBindingsDeclarations[ChannelBindings](
      "channelBindings",
      componentsMap,
      entry => {
        AsyncChannelBindingsParser(YMapEntryLike(entry)).parse()
      },
      ChannelBindingsModel
    )
  }

  private def parseBindingsDeclarations[T <: DomainElement](
      keyword: String,
      componentsMap: YMap,
      parse: YMapEntry => T,
      model: DomainElementModel
  )(implicit ctx: AsyncWebApiContext): Unit = {
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
