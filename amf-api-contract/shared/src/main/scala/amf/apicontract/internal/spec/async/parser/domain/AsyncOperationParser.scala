package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.metamodel.domain.{AbstractModel, OperationModel}
import amf.apicontract.internal.spec.async.AsyncHelper
import amf.apicontract.internal.spec.async.parser.bindings.AsyncOperationBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorOperationTrait
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeOperationParser, OasLikeTagsParser}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.SearchScope.Named
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model._

object Async20OperationParser {
  def apply(entry: YMapEntry, adopt: Operation => Operation, isTrait: Boolean = false)(implicit
      ctx: AsyncWebApiContext
  ): AsyncOperationParser =
    if (isTrait) new Async20OperationTraitParser(entry, adopt)
    else new Async20ConcreteOperationParser(entry, adopt)
}

object Async24OperationParser {
  def apply(entry: YMapEntry, adopt: Operation => Operation, isTrait: Boolean = false)(implicit
      ctx: AsyncWebApiContext
  ): AsyncOperationParser =
    if (isTrait) Async24OperationTraitParser(entry, adopt)
    else Async24ConcreteOperationParser(entry, adopt)
}

abstract class AsyncOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext
) extends OasLikeOperationParser(entry, adopt) {

  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "tags",
      entry => {
        val tags = OasLikeTagsParser(operation.id, entry).parse()
        operation.setWithoutId(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key("bindings").foreach { entry =>
      val bindings = AsyncOperationBindingsParser(YMapEntryLike(entry.value)).parse()
      operation.setWithoutId(OperationModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(operation, map).parseOrphanNode("bindings")
    }

    parseMessages(map, operation)

    parseTraits(map, operation)

    map.key("security").foreach(entry => parseSecuritySchemas(entry, operation))

    operation
  }

  override def parseOperationId(map: YMap, operation: Operation): Unit = {
    map.key("operationId", OperationModel.OperationId in operation)
  }

  protected def parseMessages(map: YMap, operation: Operation): Unit = {}

  protected def parseTraits(map: YMap, operation: Operation): Unit = {}

  protected def parseSecuritySchemas(entry: YMapEntry, operation: Operation): Unit = {}
}

class Async20ConcreteOperationParser(entry: YMapEntry, adopt: Operation => Operation)(implicit
    ctx: AsyncWebApiContext
) extends AsyncOperationParser(entry, adopt) {

  override protected def parseMessages(map: YMap, operation: Operation): Unit = map.key(
    "message",
    messageEntry =>
      AsyncHelper.messageType(entry.key.value.toString) foreach { msgType =>
        val messages = AsyncMultipleMessageParser(messageEntry.value.as[YMap], operation.id, msgType).parse()
        operation.fields
          .setWithoutId(msgType.field, AmfArray(messages, Annotations(messageEntry.value)), Annotations(messageEntry))
      }
  )

  override protected def parseTraits(map: YMap, operation: Operation): Unit =
    map.key(
      "traits",
      traitEntry => {
        val traits = traitEntry.value.as[YSequence].nodes.map { node =>
          AsyncOperationTraitRefParser(node, adopt).parseLinkOrError()
        }
        operation.fields.setWithoutId(
          OperationModel.Extends,
          AmfArray(traits, Annotations(traitEntry.value)),
          Annotations(traitEntry)
        )
      }
    )

  override protected def parseSecuritySchemas(entry: YMapEntry, operation: Operation): Unit = {}
}

class Async20OperationTraitParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncOperationParser(entry, adopt) {

  override protected val closedShapeName: String = "operationTrait"

  override def parse(): Operation = {
    val node = entry.value
    ctx.link(node) match {
      case Left(url) =>
        AsyncOperationTraitRefParser(node, adopt, Some(entryKey.toString)).parseLink(url)
      case Right(_) =>
        val operation = super.parse()
        operation.setWithoutId(OperationModel.Name, entryKey, Annotations(entry.key))
        operation.setWithoutId(AbstractModel.IsAbstract, AmfScalar(true), Annotations.synthesized())
        operation
    }
  }

  override protected def parseMessages(map: YMap, operation: Operation): Unit = {}

  override protected def parseTraits(map: YMap, operation: Operation): Unit = {}

  override protected def parseSecuritySchemas(entry: YMapEntry, operation: Operation): Unit = {}
}

case class AsyncOperationTraitRefParser(node: YNode, adopt: Operation => Operation, name: Option[String] = None)(
    implicit val ctx: AsyncWebApiContext
) {

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
    ctx.eh.violation(ParserSideValidations.ExpectedReference, "", s"Expected reference", node.location)
    new ErrorOperationTrait(name, node).link(name, Annotations(node)).asInstanceOf[Operation].withAbstract(true)
  }

  private def linkError(url: String, node: YNode): Operation = {
    ctx.eh.violation(
      CoreValidations.UnresolvedReference,
      "",
      s"Cannot find operation trait reference $url",
      node.location
    )
    val t: ErrorOperationTrait = new ErrorOperationTrait(url, node).link(url, Annotations(node))
    t

  }

  private def remote(url: String, node: YNode): Operation = {
    ctx.navigateToRemoteYNode(url) match {
      case Some(result) =>
        val operationNode = result.remoteNode
        Async20OperationParser(YMapEntry(name.getOrElse(url), operationNode), adopt, isTrait = true)(result.context)
          .parse()
      case None => linkError(url, node)
    }
  }
}

case class Async24ConcreteOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext
) extends Async20ConcreteOperationParser(entry, adopt)
    with SecuritySchemeParser {

  override protected def parseMessages(map: YMap, operation: Operation): Unit =
    super.parseMessages(map: YMap, operation: Operation)

  override protected def parseTraits(map: YMap, operation: Operation): Unit =
    super.parseTraits(map: YMap, operation: Operation)

  override protected def parseSecuritySchemas(entry: YMapEntry, operation: Operation): Unit = {
    super.parseSecuritySchemas(entry, operation)
    parseSecurityScheme(entry, OperationModel.Security, operation)
  }
}

case class Async24OperationTraitParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AsyncWebApiContext
) extends Async20OperationTraitParser(entry, adopt)
    with SecuritySchemeParser {

  override protected def parseMessages(map: YMap, operation: Operation): Unit =
    super.parseMessages(map: YMap, operation: Operation)

  override protected def parseTraits(map: YMap, operation: Operation): Unit =
    super.parseTraits(map: YMap, operation: Operation)

  override protected def parseSecuritySchemas(entry: YMapEntry, operation: Operation): Unit = {
    super.parseSecuritySchemas(entry, operation)
    parseSecurityScheme(entry, OperationModel.Security, operation)
  }
}
