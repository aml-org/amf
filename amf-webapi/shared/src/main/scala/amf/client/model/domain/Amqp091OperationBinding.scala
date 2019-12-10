package amf.client.model.domain
import amf.client.convert.WebApiClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.webapi.models.bindings.amqp.{Amqp091OperationBinding => InternalAmqp091OperationBinding}

@JSExportAll
class Amqp091OperationBinding(override private[amf] val _internal: InternalAmqp091OperationBinding)
    extends OperationBinding {

  @JSExportTopLevel("model.domain.Amqp091OperationBinding")
  def this() = this(InternalAmqp091OperationBinding())

  override def linkCopy(): Amqp091OperationBinding = _internal.linkCopy()
}
