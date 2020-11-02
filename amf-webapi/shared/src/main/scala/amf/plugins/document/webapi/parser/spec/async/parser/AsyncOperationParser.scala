package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.model.domain.AmfArray
import amf.core.parser.SearchScope.Named
import amf.core.parser._
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorOperationTrait
import amf.plugins.document.webapi.parser.spec.async.AsyncHelper
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.OasLikeTagsParser
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncOperationBindingsParser
import amf.plugins.document.webapi.parser.spec.domain.OasLikeOperationParser
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.models.{Message, Operation}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations
import org.yaml.model._

object AsyncOperationParser {
  def apply(entry: YMapEntry, parentId: String, isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext): AsyncOperationParser =
    if (isTrait) new AsyncOperationTraitParser(entry, parentId)
    else new AsyncConcreteOperationParser(entry, parentId)
}

abstract class AsyncOperationParser(entry: YMapEntry, parentId: String)(override implicit val ctx: AsyncWebApiContext)
    extends OasLikeOperationParser(entry, parentId) {

  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "tags",
      entry => {
        val tags = OasLikeTagsParser(operation.id, entry).parse()
        operation.set(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key("bindings").foreach { entry =>
      val bindings = AsyncOperationBindingsParser(YMapEntryLike(entry.value), operation.id).parse()
      operation.set(OperationModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(operation, map).parseOrphanNode("bindings")
    }

    parseMessages(map, operation)

    parseTraits(map, operation)

    operation
  }

  override def parseOperationId(map: YMap, operation: Operation): Unit = {
    map.key("operationId", OperationModel.OperationId in operation)
  }

  protected def parseMessages(map: YMap, operation: Operation)

  protected def parseTraits(map: YMap, operation: Operation)
}

private class AsyncConcreteOperationParser(entry: YMapEntry, parentId: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncOperationParser(entry, parentId) {

  override protected def parseMessages(map: YMap, operation: Operation): Unit = map.key(
    "message",
    messageEntry =>
      AsyncHelper.messageType(entry.key.value.toString) foreach { msgType =>
        val messages = AsyncMultipleMessageParser(messageEntry.value.as[YMap], operation.id, msgType).parse()
        operation.setArrayWithoutId(msgType.field, messages, Annotations(messageEntry))
    }
  )

  override protected def parseTraits(map: YMap, operation: Operation): Unit =
    map.key(
      "traits",
      traitEntry => {
        val traits = traitEntry.value.as[YSequence].nodes.map { node =>
          AsyncOperationRefParser(node, parentId).parse()

        }
        operation.setArray(OperationModel.Extends, traits, Annotations(traitEntry))
      }
    )
}

private class AsyncOperationTraitParser(entry: YMapEntry, parentId: String)(
    override implicit val ctx: AsyncWebApiContext)
    extends AsyncOperationParser(entry, parentId) {

  override def parse(): Operation = {
    val operation = super.parse()
    operation.set(OperationModel.Name, methodNode, Annotations(entry.key))
    val map = entry.value.as[YMap]
    operation.withAbstract(true)
    ctx.closedShape(operation.id, map, "operationTrait")
    operation
  }

  override protected def parseMessages(map: YMap, operation: Operation): Unit = Unit

  override protected def parseTraits(map: YMap, operation: Operation): Unit = Unit
}

case class AsyncOperationRefParser(node: YNode, parentId: String)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Operation = {
    ctx.link(node) match {
      case Left(url) =>
        val operation = parseLink(url, node)
        operation
      case Right(url) => expectedRef(node, url)
    }
  }

  private def parseLink(url: String, node: YNode): Operation = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(url, "operationTraits")
    val operation: Operation = ctx.declarations
      .findOperationTrait(label, Named)
      .map { res =>
        val resLink: Operation = res.link(label)
        resLink.add(Annotations(node))
      }
      .getOrElse(remote(url, node))
    operation
  }

  private def expectedRef(node: YNode, name: String): Operation = {
    ctx.eh.violation(ParserSideValidations.ExpectedReference, "", s"Expected reference", node)
    new ErrorOperationTrait(name, node).link(name, Annotations(node)).asInstanceOf[Operation].withAbstract(true)
  }

  private def linkError(url: String, node: YNode): Operation = {
    ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find operation trait reference $url", node)
    new ErrorOperationTrait(url, node).link(url, Annotations(node))
  }

  private def remote(url: String, node: YNode): Operation = {
    ctx.obtainRemoteYNode(url) match {
      case Some(correlationIdNode) =>
        AsyncOperationParser(YMapEntry(url, correlationIdNode), parentId, isTrait = true)
          .parse()
      case None => linkError(url, node)
    }
  }
}
