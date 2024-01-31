package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.{MessageBinding, MessageBindings}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.{Amqp, Http, Kafka, Mqtt}
import amf.apicontract.internal.spec.async.parser.bindings.message.{
  Amqp091MessageBindingParser,
  HttpMessageBindingParser,
  KafkaMessageBindingParser,
  MqttMessageBindingParser
}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorMessageBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike

object AsyncMessageBindingsParser {
  private val parserMap: Map[String, BindingParser[MessageBinding]] = Map(
    Amqp  -> Amqp091MessageBindingParser,
    Http  -> HttpMessageBindingParser,
    Kafka -> KafkaMessageBindingParser,
    Mqtt  -> MqttMessageBindingParser
  )
}

case class AsyncMessageBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {

  override protected val parsers: Map[String, BindingParser[MessageBinding]] = AsyncMessageBindingsParser.parserMap
  override type Binding  = MessageBinding
  override type Bindings = MessageBindings
  override protected val bindingsField: Field = MessageBindingsModel.Bindings

  override protected def createParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser =
    AsyncMessageBindingsParser(entryLike)

  override protected def createBindings(): MessageBindings = MessageBindings()

  def handleRef(fullRef: String): MessageBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "messageBindings")
    ctx.declarations
      .findMessageBindings(label, SearchScope.Named)
      .map(messageBindings =>
        nameAndAdopt(
          messageBindings.link(AmfScalar(label), entryLike.annotations, Annotations.synthesized()),
          entryLike.key
        )
      )
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): MessageBindings =
    new ErrorMessageBindings(fullRef, entryLike.asMap)
}
