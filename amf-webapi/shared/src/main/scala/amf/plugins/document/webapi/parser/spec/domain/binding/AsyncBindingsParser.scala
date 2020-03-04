package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfObject, AmfScalar, DomainElement, NamedDomainElement}
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.domain.binding.Bindings._
import amf.plugins.domain.webapi.metamodel.bindings.{DynamicBindingModel, EmptyBindingModel}
import amf.plugins.domain.webapi.models.bindings._
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

trait AsyncBindingsParser extends SpecParserOps {
  protected type T <: NamedDomainElement

  def parse(map: YMap, parent: String, key: Option[YNode] = None)(implicit ctx: AsyncWebApiContext): Seq[T] = {
    map.regex("^(?!x-).*").map(parseElement(_, parent, key)).toSeq
  }

  private def parseElement(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T = {
    entry.key.as[String] match {
      case Http       => parseHttp(entry, parent, key)
      case WebSockets => parseWs(entry, parent, key)
      case Kafka      => parseKafka(entry, parent, key)
      case Amqp       => parseAmqp(entry, parent, key)
      case Amqp1      => parseAmqp1(entry, parent, key)
      case Mqtt       => parseMqtt(entry, parent, key)
      case Mqtt5      => parseMqtt5(entry, parent, key)
      case Nats       => parseNats(entry, parent, key)
      case Jms        => parseJms(entry, parent, key)
      case Sns        => parseSns(entry, parent, key)
      case Sqs        => parseSqs(entry, parent, key)
      case Stomp      => parseStomp(entry, parent, key)
      case Redis      => parseRedis(entry, parent, key)
      case _          => parseDynamicBinding(entry, parent, key)
    }
  }

  protected def parseHttp(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseWs(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseKafka(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseAmqp(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseAmqp1(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseMqtt(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseMqtt5(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseNats(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseJms(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseSns(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseSqs(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseStomp(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)
  protected def parseRedis(entry: YMapEntry, parent: String, key: Option[YNode])(implicit ctx: AsyncWebApiContext): T =
    parseEmptyBinding(entry, parent, key)

  protected def parseEmptyBinding(entry: YMapEntry, parent: String, key: Option[YNode])(
      implicit ctx: AsyncWebApiContext): T = {
    val binding = EmptyBinding(Annotations(entry))

    parseType(binding, entry, EmptyBindingModel.Type, parent, key)
    validateEmptyMap(entry.value, binding.id, entry.key.as[String])

    binding.asInstanceOf[T]
  }

  protected def parseDynamicBinding(entry: YMapEntry, parent: String, key: Option[YNode])(
      implicit ctx: AsyncWebApiContext): T = {
    val binding = DynamicBinding(Annotations(entry))

    parseType(binding, entry, DynamicBindingModel.Type, parent, key)
    binding.set(DynamicBindingModel.Definition,
                DataNodeParser(entry.value, parent = Some(parent)).parse(),
                Annotations(entry.value))

    binding.asInstanceOf[T]
  }

  def nameAndAdopt(element: T, parent: String, key: Option[YNode]): Unit = {
    key foreach { k =>
      element.withName(k.as[YScalar].text)
    }
    element.adopted(parent)
  }

  private def parseType(binding: DomainElement,
                        entry: YMapEntry,
                        field: Field,
                        parent: String,
                        key: Option[YNode]): Unit =
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
