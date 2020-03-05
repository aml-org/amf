package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfScalar, DomainElement, NamedDomainElement}
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.domain.binding.Bindings._
import amf.plugins.domain.webapi.metamodel.bindings.{DynamicBindingModel, EmptyBindingModel}
import amf.plugins.domain.webapi.models.bindings._
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}
import amf.plugins.document.webapi.parser.spec.domain.ConversionHelpers._

trait AsyncBindingsParser extends SpecParserOps {
  protected type Binding
  protected type Bindings <: NamedDomainElement

  def parse(entryOrMap: Either[YMapEntry, YNode], parent: String)(implicit ctx: AsyncWebApiContext): Bindings = {
    val map: YMap = entryOrMap
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(entryOrMap, fullRef, parent)
      case Right(_) =>
        buildAndPopulate(entryOrMap, parent)
    }
  }

  protected def buildAndPopulate(entryOrMap: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): Bindings

  protected def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String, parent: String)(
      implicit ctx: AsyncWebApiContext): Bindings

  protected def nameAndAdopt(m: Bindings, entry: Option[YMapEntry], parent: String): Bindings = {
    entry foreach { e =>
      m.withName(e.key.as[YScalar].text, Annotations(e.key))
    }
    m.adopted(parent)
  }

  protected def parseElements(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Seq[Binding] = {
    map.regex("^(?!x-).*").map(parseElement(_, parent)).toSeq
  }

  private def parseElement(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding = {
    entry.key.as[String] match {
      case Http       => parseHttp(entry, parent)
      case WebSockets => parseWs(entry, parent)
      case Kafka      => parseKafka(entry, parent)
      case Amqp       => parseAmqp(entry, parent)
      case Amqp1      => parseAmqp1(entry, parent)
      case Mqtt       => parseMqtt(entry, parent)
      case Mqtt5      => parseMqtt5(entry, parent)
      case Nats       => parseNats(entry, parent)
      case Jms        => parseJms(entry, parent)
      case Sns        => parseSns(entry, parent)
      case Sqs        => parseSqs(entry, parent)
      case Stomp      => parseStomp(entry, parent)
      case Redis      => parseRedis(entry, parent)
      case _          => parseDynamicBinding(entry, parent)
    }
  }

  protected def parseHttp(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseWs(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseKafka(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseAmqp(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseAmqp1(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseMqtt(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseMqtt5(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseNats(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseJms(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseSns(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseSqs(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseStomp(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)
  protected def parseRedis(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding =
    parseEmptyBinding(entry, parent)

  protected def parseEmptyBinding(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding = {
    val binding = EmptyBinding(Annotations(entry))

    parseType(binding, entry, EmptyBindingModel.Type, parent)
    validateEmptyMap(entry.value, binding.id, entry.key.as[String])

    binding.asInstanceOf[Binding]
  }

  protected def parseDynamicBinding(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding = {
    val binding = DynamicBinding(Annotations(entry))

    parseType(binding, entry, DynamicBindingModel.Type, parent)
    binding.set(DynamicBindingModel.Definition,
                DataNodeParser(entry.value, parent = Some(parent)).parse(),
                Annotations(entry.value))

    binding.asInstanceOf[Binding]
  }

  private def parseType(binding: DomainElement, entry: YMapEntry, field: Field, parent: String): Unit =
    binding.set(field, AmfScalar(entry.key.as[String], Annotations(entry.key))).adopted(parent)

  private def validateEmptyMap(value: YNode, node: String, `type`: String)(implicit ctx: AsyncWebApiContext): Unit =
    if (value.as[YMap].entries.nonEmpty) {
      ctx.violation(ParserSideValidations.NonEmptyBindingMap,
                    node,
                    s"Reserved name binding '${`type`}' must have an empty map")
    }

  protected def parseBindingVersion(binding: BindingVersion, field: Field, map: YMap)(
      implicit ctx: AsyncWebApiContext): Unit = {
    map.key("bindingVersion", field in binding)

    // If omitted, "latest" MUST be assumed.
    if (binding.bindingVersion.isNullOrEmpty) {
      binding.set(field, AmfScalar("latest"), Annotations(SynthesizedField()))
    }
  }

  protected def parseSchema(field: Field, binding: DomainElement, entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): Unit = {
    OasTypeParser(entry, shape => shape.withName("schema").adopted(parent), JSONSchemaDraft7SchemaVersion)
      .parse()
      .foreach(binding.set(field, _, Annotations(entry)))
  }
}

object Bindings {
  val Http       = "http"
  val WebSockets = "ws"
  val Kafka      = "kafka"
  val Amqp       = "amqp"
  val Amqp1      = "amqp1"
  val Mqtt       = "mqtt"
  val Mqtt5      = "mqtt5"
  val Nats       = "nats"
  val Jms        = "jms"
  val Sns        = "sns"
  val Sqs        = "sqs"
  val Stomp      = "stomp"
  val Redis      = "redis"
}
