package amf.plugins.document.webapi.parser.spec.domain
import amf.core.annotations.{SynthesizedField, TrackedElement, VirtualObject}
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  OAS30SchemaVersion,
  OasLikeCreativeWorkParser,
  OasLikeTagsParser,
  OasTypeParser
}
import amf.plugins.document.webapi.parser.spec.domain.AsyncSchemaFormats.`oas30Schema`
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncMessageBindingsParser
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.metamodel.{
  CorrelationIdModel,
  MessageModel,
  OperationModel,
  ParameterModel,
  PayloadModel
}
import amf.plugins.domain.webapi.models.{CorrelationId, Message, Parameter, Payload, Request, Response}
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}
import amf.plugins.domain.shapes.models.ExampleTracking.tracking

sealed trait MessageType {
  def field: Field
}
case object Publish extends MessageType {
  override def field: Field = OperationModel.Request
}
case object Subscribe extends MessageType {
  override def field: Field = OperationModel.Responses
}

case class AsyncMessageParser(parent: String, rootMap: YMap, messageType: MessageType)(
    implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  def parse(): List[Message] = {
    rootMap.key("oneOf") match {
      case Some(entry) =>
        entry.value
          .as[YSequence]
          .nodes
          .map { node =>
            parseSingleMessage(node.as[YMap])
          }
          .toList
      case None => List(parseSingleMessage(rootMap))
    }
  }

  private def buildMessage(map: YMap): Message = messageType match {
    case Publish   => Request(Annotations(map))
    case Subscribe => Response(Annotations(map))
  }

  private def parseSingleMessage(map: YMap)(implicit ctx: AsyncWebApiContext): Message = {
    val message = buildMessage(map)
    message.adopted(parent)

    ctx.closedShape(message.id, map, "message")

    map.key("name", MessageModel.Name in message)
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

    map.key("correlationId", MessageModel.CorrelationId in message using (CorrelationIdParser(_, message.id).parse()))

    map.key("bindings").foreach { entry =>
      val bindings = AsyncMessageBindingsParser.parse(entry.value.as[YMap], message.id)
      message.setArray(MessageModel.Bindings, bindings, Annotations(entry))
    }

    // TODO missing parsing of traits

    val payload = Payload(Annotations(VirtualObject())).adopted(message.id)

    map.key("contentType", PayloadModel.MediaType in payload)
    map.key("schemaFormat", PayloadModel.SchemaMediaType in payload)
    parseSchema(map, payload)

    message.set(MessageModel.Payloads, AmfArray(Seq(payload)))
    AnnotationParser(message, map).parse()
    message

  }

  private def parseHeaderSchema(entry: YMapEntry, parentId: String): Option[Parameter] = {
    val param = Parameter().withName("default-parameter", Annotations(SynthesizedField())).adopted(parentId) // set default name to avoid raw validations
    val shape =
      OasTypeParser(entry, shape => shape.withName("schema").adopted(param.id), JSONSchemaDraft7SchemaVersion)
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
      val schemaVersion = getSchemaVersion(payload)
      OasTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), schemaVersion)
        .parse()
        .foreach(s => payload.set(PayloadModel.Schema, tracking(s, payload.id), Annotations(entry)))
    }
  }

  def getSchemaVersion(payload: Payload)(implicit ctx: AsyncWebApiContext): JSONSchemaVersion =
    Option(payload.schemaMediaType) match {
      case Some(format) if `oas30Schema`.contains(format.value()) =>
        OAS30SchemaVersion("schema")(ctx.eh)
      // async20 schemas are handled with draft 7. Avro schema is not supported
      case _ => JSONSchemaDraft7SchemaVersion
    }
}

object AsyncSchemaFormats {
  val `async20Schema` = List("application/vnd.aai.asyncapi;version=2.0.0",
                             "application/vnd.aai.asyncapi+json;version=2.0.0",
                             "application/vnd.aai.asyncapi+yaml;version=2.0.0")
  val `oas30Schema` = List("application/vnd.oai.openapi;version=3.0.0",
                           "application/vnd.oai.openapi+json;version=3.0.0",
                           "application/vnd.oai.openapi+yaml;version=3.0.0")
  val `draft7JsonSchema` = List("application/schema+json;version=draft-07", "application/schema+yaml;version=draft-07")
  val `avroSchema` = List("application/vnd.apache.avro;version=1.9.0",
                          "application/vnd.apache.avro+json;version=1.9.0",
                          "application/vnd.apache.avro+yaml;version=1.9.0")
}

case class CorrelationIdParser(node: YNode, parentId: String)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  def parse(): CorrelationId = {
    // missing handling of refs
    val map           = node.as[YMap]
    val correlationId = CorrelationId(map).adopted(parentId)
    map.key("description", CorrelationIdModel.Description in correlationId)
    map.key("location", CorrelationIdModel.Location in correlationId)

    AnnotationParser(correlationId, map).parse()
    ctx.closedShape(correlationId.id, map, "correlationId")
    correlationId
  }
}
