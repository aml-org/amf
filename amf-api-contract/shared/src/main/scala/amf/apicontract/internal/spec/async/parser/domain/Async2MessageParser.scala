package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.bindings.MessageBindings
import amf.apicontract.client.scala.model.domain.{Message, Payload, Request, Response}
import amf.apicontract.internal.annotations.ExampleIndex
import amf.apicontract.internal.metamodel.domain.MessageModel.IsAbstract
import amf.apicontract.internal.metamodel.domain.{MessageModel, OperationModel, PayloadModel}
import amf.apicontract.internal.spec.async.parser.bindings.AsyncMessageBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.{MessageType, Publish, Subscribe}
import amf.apicontract.internal.spec.common.WebApiDeclarations.{ErrorMessage, ErrorRequest, ErrorResponse}
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.oas.parser.domain
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar}
import amf.core.internal.annotations.{TrackedElement, VirtualElement}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.utils.IdCounter
import amf.core.internal.validation.CoreValidations
import amf.shapes.client.scala.model.domain.{AnyShape, Example, NodeShape}
import amf.shapes.internal.domain.metamodel.ExampleModel
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.parser._
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}

trait AsyncMessageParser {
  def parse(): Message
}

object Async20MessageParser {

  def apply(entryLike: YMapEntryLike, parent: String, messageType: Option[MessageType], isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext
  ): AsyncMessageParser = {
    val populator = if (isTrait) Async20MessageTraitPopulator() else Async20ConcreteMessagePopulator(parent)
    val finder    = if (isTrait) MessageTraitFinder() else MessageFinder()
    new Async20MessageParser(entryLike, parent, messageType, populator, finder, isTrait)(ctx)
  }
}

object Async21MessageParser {
  def apply(entryLike: YMapEntryLike, parent: String, messageType: Option[MessageType], isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext
  ): AsyncMessageParser = {
    val populator = if (isTrait) Async21MessageTraitPopulator() else Async21ConcreteMessagePopulator(parent)
    val finder    = if (isTrait) MessageTraitFinder() else MessageFinder()
    new Async20MessageParser(entryLike, parent, messageType, populator, finder, isTrait)(ctx)
  }
}

object Async24MessageParser {
  def apply(entryLike: YMapEntryLike, parent: String, messageType: Option[MessageType], isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext
  ): AsyncMessageParser = {
    val populator = if (isTrait) Async24MessageTraitPopulator() else Async24ConcreteMessagePopulator(parent)
    val finder    = if (isTrait) MessageTraitFinder() else MessageFinder()
    new Async20MessageParser(entryLike, parent, messageType, populator, finder, isTrait)(ctx)
  }
}

class Async20MessageParser(
    entryLike: YMapEntryLike,
    parent: String,
    messageType: Option[MessageType],
    populator: Async2MessagePopulator,
    finder: Finder[Message],
    isTrait: Boolean
)(implicit val ctx: AsyncWebApiContext)
    extends AsyncMessageParser
    with SpecParserOps {

  def parse(): Message = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(fullRef)
      case Right(_) =>
        val message = buildMessage(entryLike.annotations)
        nameAndAdopt(message, entryLike.key)
        populator.populate(map, message)
    }
  }

  private def buildMessage(annotations: Annotations): Message = messageType match {
    case Some(Publish)   => Request(annotations)
    case Some(Subscribe) => Response(annotations)
    case None            => Message(annotations)
  }

  private def buildErrorMessage(fullRef: String): Message = messageType match {
    case Some(Publish)   => ErrorRequest(fullRef, entryLike.asMap)
    case Some(Subscribe) => ErrorResponse(fullRef, entryLike.asMap)
    case None            => new ErrorMessage(fullRef, entryLike.asMap, isTrait)
  }

  def nameAndAdopt(m: Message, key: Option[YNode]): Message = {
    key foreach { k =>
      m.setWithoutId(MessageModel.Name, ScalarNode(k).string(), Annotations(k))
    }
    m
  }

  private def handleRef(fullRef: String): Message = {
    val label = finder.label(fullRef)
    finder
      .findInComponents(label, SearchScope.Named)
      .map(msg => nameAndAdopt(generateLink(label, msg, entryLike), entryLike.key))
      .getOrElse(remote(fullRef))
  }

  private def remote(fullRef: String): Message = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(result) =>
        val messageNode = result.remoteNode
        val external =
          ctx.factory.messageParser(YMapEntryLike(messageNode), parent, messageType, isTrait)(result.context).parse()
        nameAndAdopt(generateLink(fullRef, external, entryLike), entryLike.key)
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          "",
          s"Cannot find link reference $fullRef",
          Annotations(entryLike.asMap)
        )
        val errorMessage = buildErrorMessage(fullRef)
        nameAndAdopt(errorMessage.link(fullRef, errorMessage.annotations), entryLike.key)
    }
  }

  private def generateLink(label: String, effectiveTarget: Message, entryLike: YMapEntryLike): Message = {
    val message = buildMessage(entryLike.annotations)
    val hash    = s"${message.id}$label".hashCode
    message
      .withId(s"${message.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label, Annotations(entryLike.value))
  }

}

case class AsyncMultipleMessageParser(map: YMap, parent: String, messageType: MessageType)(implicit
    val ctx: AsyncWebApiContext
) {
  def parse(): List[Message] = {
    map.key("oneOf") match {
      case Some(entry) =>
        entry.value
          .as[YSequence]
          .nodes
          .zipWithIndex
          .map { case (node, index) =>
            ctx.factory.messageParser(YMapEntryLike(node), s"$parent/$index", Some(messageType)).parse()
          }
          .toList
      case None => List(ctx.factory.messageParser(YMapEntryLike(map), parent, Some(messageType)).parse())
    }
  }
}

abstract class Async2MessagePopulator()(implicit ctx: AsyncWebApiContext) extends SpecParserOps {

  def populate(map: YMap, message: Message): Message = {
    map.key("name", MessageModel.DisplayName in message)
    map.key("title", MessageModel.Title in message)
    map.key("summary", MessageModel.Summary in message)
    map.key("description", MessageModel.Description in message)

    map.key(
      "externalDocs",
      MessageModel.Documentation in message using (OasLikeCreativeWorkParser.parse(_, message.id))
    )
    map.key(
      "tags",
      entry => {
        val tags = domain.OasLikeTagsParser(message.id, entry).parse()
        message.setWithoutId(MessageModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    val examples: MessageExamples = parseExamplesFacet(map, message)
    examples.all.foreach { ex =>
      ex.annotations += TrackedElement.fromInstance(message)
    }
    if (examples.payload.nonEmpty)
      message.setWithoutId(
        MessageModel.Examples,
        AmfArray(examples.payload, Annotations.virtual()),
        Annotations.inferred()
      )
    if (examples.headers.nonEmpty)
      message.setWithoutId(
        MessageModel.HeaderExamples,
        AmfArray(examples.headers, Annotations.virtual()),
        Annotations.inferred()
      )

    map.key(
      "headers",
      entry => {
        AsyncApiTypeParser(entry, shape => shape.withName("schema"), JSONSchemaDraft7SchemaVersion)
          .parse()
          .foreach {
            case n: NodeShape =>
              message.setWithoutId(MessageModel.HeaderSchema, n, Annotations(entry))
            case _ =>
              message.setWithoutId(MessageModel.HeaderSchema, NodeShape(entry.value), Annotations(entry))

              ctx.eh.violation(
                ParserSideValidations.HeaderMustBeObject,
                message,
                ParserSideValidations.HeaderMustBeObject.message,
                entry.value.location
              )
          }
      }
    )

    map.key(
      "correlationId",
      MessageModel.CorrelationId in message using (AsyncCorrelationIdParser(_, message.id).parse())
    )

    map.key("bindings").foreach { entry =>
      val bindings: MessageBindings = AsyncMessageBindingsParser(YMapEntryLike(entry.value)).parse()
      message.setWithoutId(MessageModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(message, map).parseOrphanNode("bindings")
    }

    parseTraits(map, message)

    if (shouldParsePayloadModel(map))
      parsePayload(map, message)

    ctx.closedShape(message, map, "message")
    AnnotationParser(message, map).parse()
    message
  }

  private def parsePayload(map: YMap, message: Message) = {
    val payload = Payload(Annotations(map))

    map.key("contentType", PayloadModel.MediaType in payload)
    map.key("schemaFormat", PayloadModel.SchemaMediaType in payload)
    parseSchema(map, payload)

    message.setWithoutId(
      MessageModel.Payloads,
      AmfArray(Seq(payload), Annotations(VirtualElement())),
      Annotations(VirtualElement())
    )
  }

  private def shouldParsePayloadModel(map: YMap) = {
    val payloadMapKeys = Set("contentType", "schemaFormat", "payload")
    map.map.keySet.flatMap(_.asScalar).map(_.text).intersect(payloadMapKeys).nonEmpty
  }

  protected def parseTraits(map: YMap, message: Message): Unit

  protected def parseSchema(map: YMap, payload: Payload): Unit

  case class MessageExamples(headers: Seq[Example], payload: Seq[Example]) {
    def all: Seq[Example] = headers ++: payload
  }

  private def parseExamplesFacet(map: YMap, parent: AmfObject): MessageExamples =
    map
      .key("examples")
      .map { examplesEntry =>
        val seq     = examplesEntry.value.as[YSequence]
        val counter = new IdCounter()
        val examplePairs = seq.nodes.zipWithIndex.map { case (node, index) =>
          val messageExampleNode = node.as[YMap]
          ctx.closedShape(parent, messageExampleNode, "message examples")

          val List(headerExample, payloadExample) =
            List("headers", "payload")
              .map { key => messageExampleNode.key(key) }
              .map { maybeExampleNode =>
                maybeExampleNode
                  .map { exampleNode =>
                    parseExample(messageExampleNode, exampleNode, counter.genId("default-example"))
                      .add(ExampleIndex(index))
                  }
              }
          (headerExample, payloadExample)
        }
        val (headers, examples) = examplePairs.unzip
        MessageExamples(headers.flatten, examples.flatten)
      }
      .getOrElse(MessageExamples(Nil, Nil))

  private def parseExample(parentNode: YMap, exampleNode: YMapEntry, name: String): Example = {
    val node              = exampleNode.value
    val example           = Example(node).withName(name)
    val exampleWithNaming = addExampleNaming(parentNode, example)
    ExampleDataParser(YMapEntryLike(node), exampleWithNaming, Oas3ExampleOptions).parse()
  }

  protected def addExampleNaming(node: YMap, example: Example): Example = example

}

abstract class Async20MessagePopulator()(implicit ctx: AsyncWebApiContext) extends Async2MessagePopulator {}
abstract class Async21MessagePopulator()(implicit ctx: AsyncWebApiContext) extends Async20MessagePopulator {

  override protected def addExampleNaming(node: YMap, example: Example): Example = {
    node.key("name", (ExampleModel.DisplayName in example).allowingAnnotations)
    node.key("summary", (ExampleModel.Description in example).allowingAnnotations)

    example
  }
}

abstract class Async24MessagePopulator()(implicit ctx: AsyncWebApiContext) extends Async21MessagePopulator {

  override def populate(map: YMap, message: Message): Message = {
    super.populate(map, message)
    map.key("messageId").foreach { entry =>
      val messageId = entry.value.toString()
      if (!ctx.registerMessageId(messageId))
        ctx.eh.violation(
          ParserSideValidations.DuplicatedMessageId,
          message,
          s"Duplicated message id '$messageId'",
          entry.value.location
        )
      parseMessageId(map, message)
    }
    message
  }

  private def parseMessageId(map: YMap, message: Message): Unit = {
    map.key("messageId", MessageModel.MessageId in message)
  }

}

trait AsyncMessageTraitPopulator {
  protected def innerPopulate(map: YMap, message: Message)(implicit ctx: AsyncWebApiContext): Message = {
    message.setWithoutId(IsAbstract, AmfScalar(true), Annotations.synthesized())
    ctx.closedShape(message, map, "messageTrait")
    message
  }
}

case class Async21MessageTraitPopulator()(implicit ctx: AsyncWebApiContext)
    extends Async21MessagePopulator()
    with AsyncMessageTraitPopulator {
  override protected def parseTraits(map: YMap, message: Message): Unit = Unit

  override protected def parseSchema(map: YMap, payload: Payload): Unit = Unit

  override def populate(map: YMap, message: Message): Message = innerPopulate(map, super.populate(map, message))
}
case class Async20MessageTraitPopulator()(implicit ctx: AsyncWebApiContext)
    extends Async20MessagePopulator()
    with AsyncMessageTraitPopulator {

  override protected def parseTraits(map: YMap, message: Message): Unit = Unit

  override protected def parseSchema(map: YMap, payload: Payload): Unit = Unit

  override def populate(map: YMap, message: Message): Message = innerPopulate(map, super.populate(map, message))
}

case class Async24MessageTraitPopulator()(implicit ctx: AsyncWebApiContext)
    extends Async24MessagePopulator()
    with AsyncMessageTraitPopulator {

  override protected def parseTraits(map: YMap, message: Message): Unit = Unit

  override protected def parseSchema(map: YMap, payload: Payload): Unit = Unit

  override def populate(map: YMap, message: Message): Message = innerPopulate(map, super.populate(map, message))
}

trait AsyncConcreteMessagePopulator {
  protected def innerParseTrait(map: YMap, message: Message, parentId: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map
      .key("traits")
      .map(entry => {
        val traits = entry.value.as[YSequence].nodes.map { node =>
          ctx.factory.messageParser(YMapEntryLike(node), parentId, None, isTrait = true).parse()
        }
        message.fields
          .setWithoutId(OperationModel.Extends, AmfArray(traits, Annotations(entry.value)), Annotations(entry))
      })
  }

  protected def innerParseSchema(map: YMap, payload: Payload)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key("payload") match {
      case Some(entry) =>
        val schemaVersion = AsyncSchemaFormats.getSchemaVersion(payload)(ctx.eh)
        val maybeSchema   = AsyncApiTypeParser(entry, shape => shape.withName("schema"), schemaVersion).parse()
        payload.setWithoutId(
          PayloadModel.Schema,
          tracking(maybeSchema.getOrElse(AnyShape(Annotations(entry))), payload),
          Annotations(entry)
        )
      case None =>
        // if "payload" key is not explicit, make the Payload object a VirtualElement (W-16609870)
        payload.annotations += VirtualElement()
    }
  }
}

case class Async21ConcreteMessagePopulator(parentId: String)(implicit ctx: AsyncWebApiContext)
    extends Async21MessagePopulator()
    with AsyncConcreteMessagePopulator {
  override protected def parseTraits(map: YMap, message: Message): Unit = innerParseTrait(map, message, parentId)
  override protected def parseSchema(map: YMap, payload: Payload): Unit = innerParseSchema(map, payload)
}

case class Async20ConcreteMessagePopulator(parentId: String)(implicit ctx: AsyncWebApiContext)
    extends Async20MessagePopulator()
    with AsyncConcreteMessagePopulator() {

  override protected def parseTraits(map: YMap, message: Message): Unit = innerParseTrait(map, message, parentId)

  def parseSchema(map: YMap, payload: Payload): Unit = innerParseSchema(map, payload)
}

case class Async24ConcreteMessagePopulator(parentId: String)(implicit ctx: AsyncWebApiContext)
    extends Async24MessagePopulator()
    with AsyncConcreteMessagePopulator() {

  override protected def parseTraits(map: YMap, message: Message): Unit = innerParseTrait(map, message, parentId)

  def parseSchema(map: YMap, payload: Payload): Unit = innerParseSchema(map, payload)
}

sealed trait Finder[T] {
  def findInComponents(label: String, scope: SearchScope.Scope): Option[T]
  def label(fullRef: String): String
}

case class MessageFinder()(implicit val ctx: AsyncWebApiContext) extends Finder[Message] {
  override def findInComponents(label: String, scope: SearchScope.Scope): Option[Message] =
    ctx.declarations.findMessage(label, SearchScope.Named)

  override def label(fullRef: String): String = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messages")
}

case class MessageTraitFinder()(implicit val ctx: AsyncWebApiContext) extends Finder[Message] {
  override def findInComponents(label: String, scope: SearchScope.Scope): Option[Message] =
    ctx.declarations.findMessageTrait(label, SearchScope.Named)

  override def label(fullRef: String): String = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messageTraits")
}
