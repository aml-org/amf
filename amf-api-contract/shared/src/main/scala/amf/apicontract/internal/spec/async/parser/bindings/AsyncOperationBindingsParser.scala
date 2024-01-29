package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091OperationBinding
import amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.{OperationBinding, OperationBindings}
import amf.apicontract.internal.metamodel.domain.bindings._
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
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

case class AsyncOperationBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {
  override type Binding  = OperationBinding
  override type Bindings = OperationBindings
  override val bindingsField: Field = OperationBindingsModel.Bindings

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

  override protected def parseHttp(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    HttpOperationBindingParser.parse(entry, parent)
  }

  override protected def parseAmqp(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    Amqp091OperationBindingParser.parse(entry, parent)
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    KafkaOperationBindingParser.parse(entry, parent)
  }

  override protected def parseMqtt(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    MqttOperationBindingParser.parse(entry, parent)
  }
}
