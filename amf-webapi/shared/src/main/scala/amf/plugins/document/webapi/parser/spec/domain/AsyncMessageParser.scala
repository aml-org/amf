package amf.plugins.document.webapi.parser.spec.domain
import amf.core.annotations.{SynthesizedField, TrackedElement, VirtualObject}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.async.{MessageType, Publish, Subscribe}
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasLikeCreativeWorkParser, OasLikeTagsParser}
import amf.plugins.document.webapi.parser.spec.async.parser.{AsyncApiTypeParser, AsyncSchemaFormats}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncMessageBindingsParser
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.metamodel.{MessageModel, ParameterModel, PayloadModel}
import amf.plugins.domain.webapi.models.{Message, Parameter, Payload, Request, Response}
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.models.bindings.MessageBindings
import amf.plugins.features.validation.CoreValidations
import ConversionHelpers._
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorMessage

case class AsyncMessageParser(parent: String, messageType: Option[MessageType])(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  def parse(rootMap: YMap): List[Message] = {
    rootMap.key("oneOf") match {
      case Some(entry) =>
        entry.value
          .as[YSequence]
          .nodes
          .map { node =>
            parseSingleMessage(Right(node.as[YMap]))
          }
          .toList
      case None => List(parseSingleMessage(Right(rootMap)))
    }
  }

  def parseSingle(entryOrNode: Either[YMapEntry, YNode]): Message = parseSingleMessage(entryOrNode)

  private def buildMessage(map: YMap): Message = messageType match {
    case Some(Publish)   => Request(Annotations(map))
    case Some(Subscribe) => Response(Annotations(map))
    case None            => Message(Annotations(map))
  }

  def nameAndAdopt(m: Message, entry: Option[YMapEntry]): Message = {
    entry foreach { e =>
      m.set(MessageModel.Name, ScalarNode(e.key).string())
    }
    m.adopted(parent)
  }

  private def parseSingleMessage(entryOrNode: Either[YMapEntry, YNode])(implicit ctx: AsyncWebApiContext): Message = {
    val map: YMap = entryOrNode
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(entryOrNode, fullRef)
      case Right(_) =>
        val message = buildMessage(map)
        nameAndAdopt(message, entryOrNode.left.toOption)
        AsyncMessagePopulator(map, message).populate()
    }
  }

  private def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String): Message = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messages")
    ctx.declarations
      .findMessage(label, SearchScope.Named)
      .map(msg => nameAndAdopt(generateLink(label, msg, entryOrNode), entryOrNode.left.toOption))
      .getOrElse(remote(fullRef, entryOrNode))
  }

  private def remote(fullRef: String, entryOrNode: Either[YMapEntry, YNode]) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(messageNode) =>
        val external = AsyncMessageParser(parent, messageType).parseSingle(Right(messageNode))
        nameAndAdopt(generateLink(fullRef, external, entryOrNode), entryOrNode.left.toOption)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference,
                         "",
                         s"Cannot find link reference $fullRef",
                         Annotations(entryOrNode))
        nameAndAdopt(new ErrorMessage(fullRef, entryOrNode).link(fullRef), entryOrNode.left.toOption)
    }
  }

  private def generateLink(label: String, effectiveTarget: Message, entryOrNode: Either[YMapEntry, YNode]): Message = {
    val message = buildMessage(entryOrNode)
    val hash    = s"${message.id}$label".hashCode
    message
      .withId(s"${message.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label)
  }

}

sealed case class AsyncMessagePopulator(map: YMap, message: Message)(implicit ctx: AsyncWebApiContext)
    extends SpecParserOps {
  def populate(): Message = {
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
    val examples = parseNamedValueExamples(map, message.id)
    if (examples.nonEmpty) {
      examples.foreach { ex =>
        ex.annotations += TrackedElement(message.id)
      }
      message.set(MessageModel.Examples, AmfArray(examples))
    }

    map.key(
      "headers",
      entry =>
        parseHeaderSchema(entry, message.id) foreach { param =>
          message.withHeaders(Seq(param))
      }
    )

    map.key("correlationId",
            MessageModel.CorrelationId in message using (AsyncCorrelationIdParser(_, message.id).parse()))

    map.key("bindings").foreach { entry =>
      val bindings: MessageBindings = AsyncMessageBindingsParser.parse(Right(entry.value.as[YMap]), message.id)
      message.set(MessageModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(message, map).parseOrphanNode("bindings")
    }

    // TODO missing parsing of traits

    val payload = Payload(Annotations(VirtualObject())).adopted(message.id)

    map.key("contentType", PayloadModel.MediaType in payload)
    map.key("schemaFormat", PayloadModel.SchemaMediaType in payload)
    parseSchema(map, payload)

    ctx.closedShape(message.id, map, "message")
    message.set(MessageModel.Payloads, AmfArray(Seq(payload)))
    AnnotationParser(message, map).parse()
    message
  }

  private def parseHeaderSchema(entry: YMapEntry, parentId: String): Option[Parameter] = {
    val param = Parameter().withName("default-parameter", Annotations(SynthesizedField())).adopted(parentId) // set default name to avoid raw validations
    val shape =
      AsyncApiTypeParser(entry, shape => shape.withName("schema").adopted(param.id), JSONSchemaDraft7SchemaVersion)
        .parse()
    shape.map { schema =>
      param.set(ParameterModel.Binding, AmfScalar("header"), Annotations() += SynthesizedField())
      param.withSchema(schema)
    }
  }

  private def parseNamedValueExamples(map: YMap, parentId: String): Seq[Example] =
    map.key("examples") match {
      case Some(examplesEntry) =>
        examplesEntry.value
          .as[YMap]
          .entries
          .map(entry => {
            val example = Example(entry).adopted(parentId)
            example.set(ExampleModel.Name, ScalarNode(entry.key).text())
            RamlExampleValueAsString(entry.value, example, Oas3ExampleOptions).populate()
          })
      case None => Nil
    }

  def parseSchema(map: YMap, payload: Payload)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key("payload").foreach { entry =>
      val schemaVersion = AsyncSchemaFormats.getSchemaVersion(payload)(ctx.eh)
      AsyncApiTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), schemaVersion)
        .parse()
        .foreach(s => payload.set(PayloadModel.Schema, tracking(s, payload.id), Annotations(entry)))
    }
  }
}
