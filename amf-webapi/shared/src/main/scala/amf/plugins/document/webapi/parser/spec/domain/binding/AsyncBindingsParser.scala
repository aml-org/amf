package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.Field
import amf.core.model.domain._
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, SpecParserOps, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.domain.binding.Bindings._
import amf.plugins.domain.webapi.metamodel.bindings.{BindingType, WebSocketsChannelBindingModel}
import amf.plugins.domain.webapi.models.bindings._
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

abstract class AsyncBindingsParser(entryLike: YMapEntryLike, parent: String)(implicit ctx: AsyncWebApiContext)
    extends SpecParserOps {

  protected type Binding <: DomainElement
  protected val bindingsField: Field
  protected type Bindings <: NamedDomainElement with Linkable

  def parse(): Bindings = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) => handleRef(fullRef)
      case Right(_)      => buildAndPopulate()
    }
  }

  protected def createParser(entryLike: YMapEntryLike): AsyncBindingsParser

  def buildAndPopulate(): Bindings = {
    val map: YMap          = entryLike.asMap
    val bindings: Bindings = createBindings()
    nameAndAdopt(bindings, entryLike.key)
    ctx.closedShape(bindings.id, map, "bindings")
    parseBindings(bindings, map)
  }

  protected def parseBindings(obj: Bindings, map: YMap): Bindings = {
    val bindings: Seq[Binding] = parseElements(map, obj.id)
    obj.set(bindingsField, AmfArray(bindings, Annotations(map)), Annotations(map))
  }

  protected def createBindings(): Bindings

  protected def handleRef(fullRef: String): Bindings

  protected def nameAndAdopt(m: Bindings, key: Option[YNode]): Bindings = {
    key foreach { k =>
      m.withName(k.as[YScalar].text, Annotations(k))
    }
    m.adopted(parent).add(entryLike.annotations)
  }

  protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): Bindings

  protected def remote(fullRef: String, entryLike: YMapEntryLike, parent: String)(
      implicit ctx: AsyncWebApiContext): Bindings = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(bindingsNode) =>
        val external = createParser(YMapEntryLike(bindingsNode)).parse()
        nameAndAdopt(external.link(fullRef), entryLike.key)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference,
                         "",
                         s"Cannot find link reference $fullRef",
                         entryLike.asMap)
        val errorBinding = errorBindings(fullRef, entryLike)
        nameAndAdopt(errorBinding.link(fullRef, errorBinding.annotations), entryLike.key)
    }
  }

  protected def parseElements(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Seq[Binding] = {
    map.regex("^(?!x-).*").flatMap(parseElement(_, parent)).toSeq
  }

  private def parseElement(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Option[Binding] = {
    val bindingOption = entry.key.as[String] match {
      case Http       => Some(parseHttp(entry, parent))
      case WebSockets => Some(parseWs(entry, parent))
      case Kafka      => Some(parseKafka(entry, parent))
      case Amqp       => Some(parseAmqp(entry, parent))
      case Amqp1      => Some(parseAmqp1(entry, parent))
      case Mqtt       => Some(parseMqtt(entry, parent))
      case Mqtt5      => Some(parseMqtt5(entry, parent))
      case Nats       => Some(parseNats(entry, parent))
      case Jms        => Some(parseJms(entry, parent))
      case Sns        => Some(parseSns(entry, parent))
      case Sqs        => Some(parseSqs(entry, parent))
      case Stomp      => Some(parseStomp(entry, parent))
      case Redis      => Some(parseRedis(entry, parent))
      case _          => None
    }
    bindingOption.map(setBindingType(entry, _))
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

    validateEmptyMap(entry.value, binding.id, entry.key.as[String])

    binding.asInstanceOf[Binding]
  }

  private def validateEmptyMap(value: YNode, node: String, `type`: String)(implicit ctx: AsyncWebApiContext): Unit =
    if (value.as[YMap].entries.nonEmpty) {
      ctx.eh.violation(ParserSideValidations.NonEmptyBindingMap,
                       node,
                       s"Reserved name binding '${`type`}' must have an empty map",
                       value)
    }

  protected def parseBindingVersion(binding: BindingVersion, field: Field, map: YMap)(
      implicit ctx: AsyncWebApiContext): Unit = {
    map.key("bindingVersion", field in binding)

    if (bindingVersionIsEmpty(binding)) setDefaultBindingVersionValue(binding, field)
  }

  private def setDefaultBindingVersionValue(binding: BindingVersion, field: Field) = {
    binding.set(field, AmfScalar("latest"), Annotations(SynthesizedField()))
  }

  private def bindingVersionIsEmpty(binding: BindingVersion) = {
    binding.bindingVersion.isNullOrEmpty
  }

  protected def parseSchema(field: Field, binding: DomainElement, entry: YMapEntry, parent: String)(
      implicit ctx: AsyncWebApiContext): Unit = {
    OasTypeParser(entry, shape => shape.withName("schema"), JSONSchemaDraft7SchemaVersion)
      .parse()
      .foreach { shape =>
        binding.set(field, shape, Annotations(entry))
        shape.adopted(parent)
      }
  }

  private def setBindingType(entry: YMapEntry, binding: Binding): Binding = {
    val node = ScalarNode(entry.key)
    binding.set(BindingType.Type, node.string(), Annotations(entry.key))
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
