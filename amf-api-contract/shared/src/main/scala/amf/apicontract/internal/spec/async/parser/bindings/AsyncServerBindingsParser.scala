package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.{ServerBinding, ServerBindings}
import amf.apicontract.internal.metamodel.domain.bindings.ServerBindingsModel
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.{IBMMQ, Mqtt}
import amf.apicontract.internal.spec.async.parser.bindings.server._
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorServerBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike

object AsyncServerBindingsParser {
  private val parserMap: Map[String, BindingParser[ServerBinding]] = Map(
    Mqtt  -> MqttServerBindingParser,
    IBMMQ -> IBMMQServerBindingParser
  )
}
case class AsyncServerBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {

  override type Binding  = ServerBinding
  override type Bindings = ServerBindings
  override val bindingsField: Field                                         = ServerBindingsModel.Bindings
  override protected val parsers: Map[String, BindingParser[ServerBinding]] = AsyncServerBindingsParser.parserMap

  override protected def createParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser =
    AsyncServerBindingsParser(entryLike)

  override protected def createBindings(): ServerBindings = ServerBindings()

  def handleRef(fullRef: String): ServerBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "serverBindings")
    ctx.declarations
      .findServerBindings(label, SearchScope.Named)
      .map(serverBindings =>
        nameAndAdopt(
          serverBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
          entryLike.key
        )
      )
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): ServerBindings =
    new ErrorServerBindings(fullRef, entryLike.asMap)
}
