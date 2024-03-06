package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.{OperationBinding, OperationBindings}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.spec.async.parser.bindings.AsyncOperationBindingsParser.parserMap
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.{Amqp, Http, Kafka, Mqtt}
import amf.apicontract.internal.spec.async.parser.bindings.operation.{
  Amqp091OperationBindingParser,
  HttpOperationBindingParser,
  KafkaOperationBindingParser,
  MqttOperationBindingParser
}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorOperationBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike

object AsyncOperationBindingsParser {
  private val parserMap: Map[String, BindingParser[OperationBinding]] = Map(
    Amqp  -> Amqp091OperationBindingParser,
    Http  -> HttpOperationBindingParser,
    Kafka -> KafkaOperationBindingParser,
    Mqtt  -> MqttOperationBindingParser
  )
}

case class AsyncOperationBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {

  override type Binding  = OperationBinding
  override type Bindings = OperationBindings
  override val bindingsField: Field                                            = OperationBindingsModel.Bindings
  override protected val parsers: Map[String, BindingParser[OperationBinding]] = parserMap

  override protected def createBindings(): OperationBindings = OperationBindings()

  protected def createParser(entryOrMap: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser =
    AsyncOperationBindingsParser(entryOrMap)

  def handleRef(fullRef: String): OperationBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "operationBindings")
    ctx.declarations
      .findOperationBindings(label, SearchScope.Named)
      .map(operationBindings =>
        nameAndAdopt(
          operationBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
          entryLike.key
        )
      )
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): OperationBindings =
    new ErrorOperationBindings(fullRef, entryLike.asMap)
}
