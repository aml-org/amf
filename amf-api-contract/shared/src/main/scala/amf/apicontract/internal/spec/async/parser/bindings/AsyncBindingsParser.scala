package amf.apicontract.internal.spec.async.parser.bindings

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.validation.CoreValidations
import amf.apicontract.internal.metamodel.domain.bindings.BindingType
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

abstract class AsyncBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext) extends SpecParserOps {

  protected type Binding <: DomainElement
  protected val bindingsField: Field
  protected type Bindings <: NamedDomainElement with Linkable
  protected val parsers: Map[String, BindingParser[Binding]]

  def parse(): Bindings = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) => handleRef(fullRef)
      case Right(_)      => buildAndPopulate()
    }
  }

  protected def createParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser

  def buildAndPopulate(): Bindings = {
    val map: YMap          = entryLike.asMap
    val bindings: Bindings = createBindings()
    nameAndAdopt(bindings, entryLike.key)
    ctx.closedShape(bindings, map, "bindings")
    parseBindings(bindings, map)
  }

  protected def parseBindings(obj: Bindings, map: YMap): Bindings = {
    val bindings: Seq[Binding] = parseElements(map, obj.id)
    obj.setWithoutId(bindingsField, AmfArray(bindings, Annotations(map)), Annotations(map))
  }

  protected def createBindings(): Bindings

  protected def handleRef(fullRef: String): Bindings

  protected def nameAndAdopt(m: Bindings, key: Option[YNode]): Bindings = {
    key foreach { k =>
      m.withName(k.as[YScalar].text, Annotations(k))
    }
    m.add(entryLike.annotations)
  }

  protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): Bindings

  protected def remote(fullRef: String, entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): Bindings = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(result) =>
        val bindingsNode = result.remoteNode
        val external     = createParser(YMapEntryLike(bindingsNode))(result.context).parse()
        nameAndAdopt(
          external.link(AmfScalar(fullRef), entryLike.annotations, Annotations.synthesized()),
          entryLike.key
        ) // check if this link should be trimmed to just the label
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          "",
          s"Cannot find link reference $fullRef",
          entryLike.asMap.location
        )
        val errorBinding = errorBindings(fullRef, entryLike)
        nameAndAdopt(errorBinding.link(fullRef, errorBinding.annotations), entryLike.key)
    }
  }

  private def canParse(binding: String) = ctx.validBindingSet().canParse(binding)

  protected def parseElements(map: YMap, parent: String)(implicit ctx: AsyncWebApiContext): Seq[Binding] = {
    map.regex("^(?!x-).*").flatMap(parseElement(_, parent)).toSeq
  }

  private def parseElement(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Option[Binding] = {
    val bindingType = entry.key.as[String]
    if (canParse(bindingType)) {
      val binding: Binding = parsers
        .get(bindingType)
        .map(_.parse(entry, parent))
        .getOrElse(parseEmptyBinding(entry, parent))
      Some(setBindingType(entry, binding))
    } else None // TODO: maybe we should throw an error here? I'm not changing behaviour
  }

  protected def parseEmptyBinding(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding = {
    EmptyBindingParser.parse(entry, parent).asInstanceOf[Binding]
  }

  private def setBindingType(entry: YMapEntry, binding: Binding): Binding = {
    val node = ScalarNode(entry.key)
    binding.setWithoutId(BindingType.Type, node.string(), Annotations(entry.key))
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
  val Mercure    = "mercure"
  val IBMMQ      = "ibmmq"
  val AnypointMQ = "anypointmq"
}
