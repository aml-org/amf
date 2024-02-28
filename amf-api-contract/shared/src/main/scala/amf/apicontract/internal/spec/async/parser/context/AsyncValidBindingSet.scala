package amf.apicontract.internal.spec.async.parser.context

import amf.apicontract.internal.spec.async.parser.bindings.Bindings._

case class AsyncValidBindingSet(bindings: Set[String]) {
  def add(bindings: String*): AsyncValidBindingSet = copy(this.bindings ++ bindings)
  def canParse(binding: String): Boolean           = bindings.contains(binding)
}

object AsyncValidBindingSet {

  private val basic = Set(Http, WebSockets, Kafka, Amqp, Amqp1, Mqtt, Mqtt5, Nats, Jms, Sns, Sqs, Stomp, Redis)
  val async20: AsyncValidBindingSet = AsyncValidBindingSet(basic)
  val async21: AsyncValidBindingSet = async20.add(Mercure, IBMMQ)
  val async22: AsyncValidBindingSet = async21.add(AnypointMQ)
  val async23: AsyncValidBindingSet = async22
  val async24: AsyncValidBindingSet = async23
  val async25: AsyncValidBindingSet = async24
  val async26: AsyncValidBindingSet = async25
}
