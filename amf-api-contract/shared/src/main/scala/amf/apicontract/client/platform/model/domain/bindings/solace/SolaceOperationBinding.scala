package amf.apicontract.client.platform.model.domain.bindings.solace

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceOperationBinding => InternalSolaceOperationBinding, SolaceOperationBinding010 => InternalSolaceOperationBinding010, SolaceOperationBinding020 => InternalSolaceOperationBinding020, SolaceOperationBinding030 => InternalSolaceOperationBinding030, SolaceOperationDestination => InternalSolaceOperationDestination, SolaceOperationDestination010 => InternalSolaceOperationDestination010, SolaceOperationDestination020 => InternalSolaceOperationDestination020, SolaceOperationDestination030 => InternalSolaceOperationDestination030, SolaceOperationQueue => InternalSolaceOperationQueue, SolaceOperationQueue010 => InternalSolaceOperationQueue010, SolaceOperationQueue030 => InternalSolaceOperationQueue030, SolaceOperationTopic => InternalSolaceOperationTopic}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
// Operation
@JSExportAll
abstract class SolaceOperationBinding(
    override private[amf] val _internal: InternalSolaceOperationBinding
) extends OperationBinding
    with BindingVersion {

//  def destinations: ClientList[SolaceOperationDestination] = _internal.destinations.asClient

//  def withDestinations(destinations: ClientList[SolaceOperationDestination]): this.type = {
//    _internal.withDestinations(destinations.asInternal)
//    this
//  }
  override protected def bindingVersion: StrField = _internal.bindingVersion
  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }
}
@JSExportAll
case class SolaceOperationBinding010(override private[amf] val _internal: InternalSolaceOperationBinding010)
    extends SolaceOperationBinding(_internal) {
  @JSExportTopLevel("SolaceOperationBinding010")
  def this() = this(InternalSolaceOperationBinding010())

  def destinations: ClientList[SolaceOperationDestination010] = _internal.destinations.asClient

  def withDestinations(destinations: ClientList[SolaceOperationDestination010]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding010(
    _internal.linkCopy().asInstanceOf[InternalSolaceOperationBinding010]
  )
}
@JSExportAll
case class SolaceOperationBinding020(override private[amf] val _internal: InternalSolaceOperationBinding020)
    extends SolaceOperationBinding(_internal) {
  @JSExportTopLevel("SolaceOperationBinding020")
  def this() = this(InternalSolaceOperationBinding020())

  def destinations: ClientList[SolaceOperationDestination020] = _internal.destinations.asClient

  def withDestinations(destinations: ClientList[SolaceOperationDestination020]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding020(
    _internal.linkCopy().asInstanceOf[InternalSolaceOperationBinding020])
}
@JSExportAll
case class SolaceOperationBinding030(override private[amf] val _internal: InternalSolaceOperationBinding030)
    extends SolaceOperationBinding(_internal) {
  @JSExportTopLevel("SolaceOperationBinding030")
  def this() = this(InternalSolaceOperationBinding030())

  def destinations: ClientList[SolaceOperationDestination030] = _internal.destinations.asClient

  def withDestinations(destinations: ClientList[SolaceOperationDestination030]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }
  override def linkCopy(): SolaceOperationBinding = SolaceOperationBinding030(
    _internal.linkCopy().asInstanceOf[InternalSolaceOperationBinding030])
}

// Operation Destination
@JSExportAll
abstract class SolaceOperationDestination(override private[amf] val _internal: InternalSolaceOperationDestination)
    extends DomainElement {

  def destinationType: StrField = _internal.destinationType
  def deliveryMode: StrField    = _internal.deliveryMode

  def withDestinationType(destinationType: String): this.type = {
    _internal.withDestinationType(destinationType)
    this
  }
  def withDeliveryMode(deliveryMode: String): this.type = {
    _internal.withDeliveryMode(deliveryMode)
    this
  }


}

@JSExportAll
case class SolaceOperationDestination010(override private[amf] val _internal: InternalSolaceOperationDestination010)
    extends SolaceOperationDestination(_internal) {
  def queue: SolaceOperationQueue010 = _internal.queue

  def withQueue(queue: SolaceOperationQueue010): this.type = {
    _internal.withQueue(queue)
    this
  }
  @JSExportTopLevel("SolaceOperationDestination010")
  def this() = this(InternalSolaceOperationDestination010())

}

@JSExportAll
case class SolaceOperationDestination020(override private[amf] val _internal: InternalSolaceOperationDestination020)
    extends SolaceOperationDestination(_internal) {
  @JSExportTopLevel("SolaceOperationDestination020")
  def this() = this(InternalSolaceOperationDestination020())
  def queue: SolaceOperationQueue010= _internal.queue
  def withQueue(queue: SolaceOperationQueue010): this.type = {
    _internal.withQueue(queue)
    this
  }
  def topic: SolaceOperationTopic = _internal.topic
  def withTopic(topic: SolaceOperationTopic): this.type = {
    _internal.withTopic(topic)
    this
  }
  def destinations: ClientList[SolaceOperationDestination020] = _internal.destinations.asClient
  def withDestinations(destinations: ClientList[SolaceOperationDestination020]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }
}
@JSExportAll
case class SolaceOperationDestination030(override private[amf] val _internal: InternalSolaceOperationDestination030)
    extends SolaceOperationDestination(_internal)
    {
  @JSExportTopLevel("SolaceOperationDestination030")
  def this() = this(InternalSolaceOperationDestination030())
  def queue: SolaceOperationQueue030= _internal.queue
  def withQueue(queue: SolaceOperationQueue030): this.type = {
    _internal.withQueue(queue)
    this
  }
  def topic: SolaceOperationTopic = _internal.topic
  def withTopic(topic: SolaceOperationTopic): this.type = {
    _internal.withTopic(topic)
    this
  }
  def destinations: ClientList[SolaceOperationDestination030] = _internal.destinations.asClient
  def withDestinations(destinations: ClientList[SolaceOperationDestination030]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }
}

// Operation Queue
@JSExportAll
abstract class SolaceOperationQueue(override private[amf] val _internal: InternalSolaceOperationQueue)
    extends DomainElement
    with NamedDomainElement {

  def topicSubscriptions: ClientList[StrField] = _internal.topicSubscriptions.asClient
  def accessType: StrField                     = _internal.accessType

  def withTopicSubscriptions(topicSubscriptions: ClientList[String]): this.type = {
    _internal.withTopicSubscriptions(topicSubscriptions.asInternal)
    this
  }

  def withAccessType(accessType: String): this.type = {
    _internal.withAccessType(accessType)
    this
  }

  override def name: StrField = _internal.name

  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
@JSExportAll
case class SolaceOperationQueue010(override private[amf] val _internal: InternalSolaceOperationQueue010)
    extends SolaceOperationQueue(_internal) {
  @JSExportTopLevel("SolaceOperationQueue010")
  def this() = this(InternalSolaceOperationQueue010())
}
@JSExportAll
case class SolaceOperationQueue030(override private[amf] val _internal: InternalSolaceOperationQueue030)
    extends SolaceOperationQueue(_internal) {
  @JSExportTopLevel("SolaceOperationQueue030")
  def this() = this(InternalSolaceOperationQueue030())
  def maxMsgSpoolSize: StrField = _internal.maxMsgSpoolSize
  def maxTtl: StrField          = _internal.maxTtl

  def withMaxMsgSpoolSize(maxMsgSpoolSize: String): this.type = {
    _internal.withMaxMsgSpoolSize(maxMsgSpoolSize)
    this
  }
  def withMaxTtl(maxTtl: String): this.type = {
    _internal.withMaxTtl(maxTtl)
    this
  }
}

// Operation Topic
@JSExportAll
case class SolaceOperationTopic(override private[amf] val _internal: InternalSolaceOperationTopic)
    extends DomainElement {

  @JSExportTopLevel("SolaceOperationTopic")
  def this() = this(InternalSolaceOperationTopic())

  def topicSubscriptions: ClientList[StrField] = _internal.topicSubscriptions.asClient

  def withTopicSubscriptions(topicSubscriptions: ClientList[String]): this.type = {
    _internal.withTopicSubscriptions(topicSubscriptions.asInternal)
    this
  }
}
