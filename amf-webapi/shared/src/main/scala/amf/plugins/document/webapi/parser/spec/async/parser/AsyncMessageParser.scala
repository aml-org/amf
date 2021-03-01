package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.annotations.{SynthesizedField, TrackedElement, VirtualElement}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, SearchScope, YMapOps}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.annotations.ExampleIndex
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorMessage
import amf.plugins.document.webapi.parser.spec.async.{MessageType, Publish, Subscribe}
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft7SchemaVersion,
  OasLikeCreativeWorkParser,
  OasLikeTagsParser
}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncMessageBindingsParser
import amf.plugins.document.webapi.parser.spec.domain.{ExampleDataParser, Oas3ExampleOptions}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.document.webapi.parser.spec.domain.{ExampleDataParser, ExamplesDataParser, Oas3ExampleOptions}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{Example, NodeShape}
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.MessageModel.IsAbstract
import amf.plugins.domain.webapi.metamodel.{MessageModel, ParameterModel, PayloadModel}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.bindings.MessageBindings
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}

object AsyncMessageParser {

  def apply(entryLike: YMapEntryLike, parent: String, messageType: Option[MessageType], isTrait: Boolean = false)(
      implicit ctx: AsyncWebApiContext): AsyncMessageParser = {
    val populator = if (isTrait) AsyncMessageTraitPopulator() else AsyncConcreteMessagePopulator(parent)
    val finder    = if (isTrait) MessageTraitFinder() else MessageFinder()
    new AsyncMessageParser(entryLike, parent, messageType, populator, finder, isTrait)(ctx)
  }
}

class AsyncMessageParser(entryLike: YMapEntryLike,
                         parent: String,
                         messageType: Option[MessageType],
                         populator: AsyncMessagePopulator,
                         finder: Finder[Message],
                         isTrait: Boolean)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

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

  def nameAndAdopt(m: Message, key: Option[YNode]): Message = {
    key foreach { k =>
      m.set(MessageModel.Name, ScalarNode(k).string(), Annotations(k))
    }
    m.adopted(parent)
  }

  private def handleRef(fullRef: String): Message = {
    val label = finder.label(fullRef)
    finder
      .findInComponents(label, SearchScope.Named)
      .map(msg => nameAndAdopt(generateLink(label, msg, entryLike), entryLike.key))
      .getOrElse(remote(fullRef))
  }

  private def remote(fullRef: String): Message = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(messageNode) =>
        val external = AsyncMessageParser(YMapEntryLike(messageNode), parent, messageType).parse()
        nameAndAdopt(generateLink(fullRef, external, entryLike), entryLike.key)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference,
                         "",
                         s"Cannot find link reference $fullRef",
                         Annotations(entryLike.asMap))
        val errorMessage = new ErrorMessage(fullRef, entryLike.asMap, isTrait)
        nameAndAdopt(errorMessage.link(fullRef, errorMessage.annotations), entryLike.key)
    }
  }

  private def generateLink(label: String, effectiveTarget: Message, entryLike: YMapEntryLike): Message = {
    val message = buildMessage(entryLike.annotations)
    val hash    = s"${message.id}$label".hashCode
    message
      .withId(s"${message.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label, extractRefAnnotation(entryLike))
  }

}

case class AsyncMultipleMessageParser(map: YMap, parent: String, messageType: MessageType)(
    implicit val ctx: AsyncWebApiContext) {
  def parse(): List[Message] = {
    map.key("oneOf") match {
      case Some(entry) =>
        entry.value
          .as[YSequence]
          .nodes
          .zipWithIndex
          .map {
            case (node, index) =>
              AsyncMessageParser(YMapEntryLike(node), s"$parent/$index", Some(messageType)).parse()
          }
          .toList
      case None => List(AsyncMessageParser(YMapEntryLike(map), parent, Some(messageType)).parse())
    }
  }
}

abstract class AsyncMessagePopulator()(implicit ctx: AsyncWebApiContext) extends SpecParserOps {

  def populate(map: YMap, message: Message): Message = {
    map.key("name", MessageModel.DisplayName in message)
    map.key("title", MessageModel.Title in message)
    map.key("summary", MessageModel.Summary in message)
    map.key("description", MessageModel.Description in message)

    map.key("externalDocs",
            MessageModel.Documentation in message using (OasLikeCreativeWorkParser.parse(_, message.id)))
    map.key(
      "tags",
      entry => {
        val tags = OasLikeTagsParser(message.id, entry).parse()
        message.set(MessageModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    val examples: MessageExamples = parseExamplesFacet(map, message.id)
    examples.all.foreach { ex =>
      ex.annotations += TrackedElement(message.id)
    }
    if (examples.payload.nonEmpty)
      message.set(MessageModel.Examples, AmfArray(examples.payload))
    if (examples.headers.nonEmpty)
      message.set(MessageModel.HeaderExamples, AmfArray(examples.headers))

    map.key(
      "headers",
      entry => {
        AsyncApiTypeParser(entry, shape => shape.withName("schema").adopted(message.id), JSONSchemaDraft7SchemaVersion)
          .parse()
          .foreach {
            case n: NodeShape =>
              message.set(MessageModel.HeaderSchema, n, Annotations(entry))
            case _ =>
              message.set(MessageModel.HeaderSchema, NodeShape(Annotations.virtual()), Annotations(entry))

              ctx.eh.violation(ParserSideValidations.HeaderMustBeObject,
                               message.id,
                               ParserSideValidations.HeaderMustBeObject.message,
                               entry.value)
          }
      }
    )

    map.key("correlationId",
            MessageModel.CorrelationId in message using (AsyncCorrelationIdParser(_, message.id).parse()))

    map.key("bindings").foreach { entry =>
      val bindings: MessageBindings = AsyncMessageBindingsParser(YMapEntryLike(entry.value), message.id).parse()
      message.set(MessageModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(message, map).parseOrphanNode("bindings")
    }

    parseTraits(map, message)

    if (shouldParsePayloadModel(map))
      parsePayload(map, message)

    ctx.closedShape(message.id, map, "message")
    AnnotationParser(message, map).parse()
    message
  }

  private def parsePayload(map: YMap, message: Message) = {
    val payload = Payload(Annotations(map)).adopted(message.id)

    map.key("contentType", PayloadModel.MediaType in payload)
    map.key("schemaFormat", PayloadModel.SchemaMediaType in payload)
    parseSchema(map, payload)

    message.set(MessageModel.Payloads,
                AmfArray(Seq(payload), Annotations(VirtualElement())),
                Annotations(VirtualElement()))
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

  private def parseExamplesFacet(map: YMap, parentId: String): MessageExamples =
    map
      .key("examples")
      .map { examplesEntry =>
        val seq     = examplesEntry.value.as[YSequence]
        val counter = new IdCounter()
        val examplePairs = seq.nodes.zipWithIndex.map {
          case (node, index) =>
            val map = node.as[YMap]
            ctx.closedShape(parentId, map, "message examples")
            val List(headerExample, payloadExample) = List("headers", "payload").map { key =>
              map.key(key).map { n =>
                parseExample(n, counter.genId("default-example"), parentId).add(ExampleIndex(index))
              }
            }
            (headerExample, payloadExample)
        }
        val (headers, examples) = examplePairs.unzip
        MessageExamples(headers.flatten, examples.flatten)
      }
      .getOrElse(MessageExamples(Nil, Nil))

  private def parseExample(n: YMapEntry, name: String, parentId: String): Example = {
    val node = n.value
    val exa  = Example(node).withName(name)
    exa.adopted(parentId)
    ExampleDataParser(node, exa, Oas3ExampleOptions).parse()
  }
}

case class AsyncMessageTraitPopulator()(implicit ctx: AsyncWebApiContext) extends AsyncMessagePopulator() {

  override protected def parseTraits(map: YMap, message: Message): Unit = Unit

  override protected def parseSchema(map: YMap, payload: Payload): Unit = Unit

  override def populate(map: YMap, message: Message): Message = {
    val nextMessage = super.populate(map, message)
    nextMessage.set(IsAbstract, true, Annotations.synthesized())
    ctx.closedShape(nextMessage.id, map, "messageTrait")
    nextMessage
  }
}

case class AsyncConcreteMessagePopulator(parentId: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncMessagePopulator() {

  override protected def parseTraits(map: YMap, message: Message): Unit = {
    map
      .key("traits")
      .map(entry => {
        val traits = entry.value.as[YSequence].nodes.map { node =>
          AsyncMessageParser(YMapEntryLike(node), parentId, None, isTrait = true).parse()
        }
        message.setArray(MessageModel.Extends, traits, Annotations(entry))
      })
  }

  def parseSchema(map: YMap, payload: Payload): Unit = {
    map.key("payload").foreach { entry =>
      val schemaVersion = AsyncSchemaFormats.getSchemaVersion(payload)(ctx.eh)
      AsyncApiTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), schemaVersion)
        .parse()
        .foreach(s => payload.set(PayloadModel.Schema, tracking(s, payload.id), Annotations(entry)))
    }
  }
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
