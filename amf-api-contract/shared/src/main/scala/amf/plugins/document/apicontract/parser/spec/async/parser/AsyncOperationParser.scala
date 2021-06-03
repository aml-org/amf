package amf.plugins.document.apicontract.parser.spec.async.parser

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.SearchScope.Named
import amf.core.parser._
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorOperationTrait
import amf.plugins.document.apicontract.parser.spec.async.AsyncHelper
import amf.plugins.document.apicontract.parser.spec.common.AnnotationParser
import amf.plugins.document.apicontract.parser.spec.declaration.OasLikeTagsParser
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.apicontract.parser.spec.domain.binding.AsyncOperationBindingsParser
import amf.plugins.document.apicontract.parser.spec.domain.OasLikeOperationParser
import amf.plugins.domain.apicontract.metamodel.{AbstractModel, OperationModel}
import amf.plugins.domain.apicontract.models.{Message, Operation}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations
import org.yaml.model._

object AsyncOperationParser {
  def apply(entry: YMapEntry, adopt: Operation => Operation, isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext): AsyncOperationParser =
    if (isTrait) new AsyncOperationTraitParser(entry, adopt)
    else new AsyncConcreteOperationParser(entry, adopt)
}

abstract class AsyncOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext)
    extends OasLikeOperationParser(entry, adopt) {

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

      AnnotationParser(operation, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("bindings")
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

private class AsyncConcreteOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    implicit ctx: AsyncWebApiContext)
    extends AsyncOperationParser(entry, adopt) {

  override protected def parseMessages(map: YMap, operation: Operation): Unit = map.key(
    "message",
    messageEntry =>
      AsyncHelper.messageType(entry.key.value.toString) foreach { msgType =>
        val messages = AsyncMultipleMessageParser(messageEntry.value.as[YMap], operation.id, msgType).parse()
        operation.fields.setWithoutId(msgType.field,
                                      AmfArray(messages, Annotations(messageEntry.value)),
                                      Annotations(messageEntry))
    }
  )

  override protected def parseTraits(map: YMap, operation: Operation): Unit =
    map.key(
      "traits",
      traitEntry => {
        val traits = traitEntry.value.as[YSequence].nodes.map { node =>
          AsyncOperationTraitRefParser(node, adopt).parseLinkOrError()
        }
        operation.fields.set(operation.id,
                             OperationModel.Extends,
                             AmfArray(traits, Annotations(traitEntry.value)),
                             Annotations(traitEntry))
      }
    )
}

private class AsyncOperationTraitParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext)
    extends AsyncOperationParser(entry, adopt) {

  override protected val closedShapeName: String = "operationTrait"

  override def parse(): Operation = {
    val node = entry.value
    ctx.link(node) match {
      case Left(url) =>
        AsyncOperationTraitRefParser(node, adopt, Some(entryKey.toString)).parseLink(url)
      case Right(_) =>
        val operation = super.parse()
        operation.set(OperationModel.Name, entryKey, Annotations(entry.key))
        operation.set(AbstractModel.IsAbstract, AmfScalar(true), Annotations.synthesized())
        operation
    }
  }

  override protected def parseMessages(map: YMap, operation: Operation): Unit = Unit

  override protected def parseTraits(map: YMap, operation: Operation): Unit = Unit
}

case class AsyncOperationTraitRefParser(node: YNode, adopt: Operation => Operation, name: Option[String] = None)(
    implicit val ctx: AsyncWebApiContext) {

  def parseLinkOrError(): Operation = {
    ctx.link(node) match {
      case Left(url)  => parseLink(url)
      case Right(url) => expectedRef(node, url)
    }
  }

  def parseLink(url: String): Operation = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(url, "operationTraits")
    val operation: Operation = ctx.declarations
      .findOperationTrait(label, Named)
      .map { res =>
        val resLink: Operation = res.link(AmfScalar(label), Annotations(node), Annotations.synthesized())
        resLink
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
    val t: ErrorOperationTrait = new ErrorOperationTrait(url, node).link(url, Annotations(node))
    t

  }

  private def remote(url: String, node: YNode): Operation = {
    ctx.obtainRemoteYNode(url) match {
      case Some(traitNode) =>
        AsyncOperationParser(YMapEntry(name.getOrElse(url), traitNode), adopt, isTrait = true)
          .parse()
      case None => linkError(url, node)
    }
  }
}
