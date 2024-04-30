package amf.apicontract.client.platform.model.domain.bindings.solace

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, OperationBinding}
import amf.apicontract.client.scala.model.domain.bindings.solace.{
  SolaceOperationBinding => InternalSolaceOperationBinding,
  SolaceOperationDestination => InternalSolaceOperationDestination,
  SolaceOperationQueue => InternalSolaceOperationQueue,
  SolaceOperationTopic => InternalSolaceOperationTopic,
  SolaceOperationDestination010 => InternalSolaceOperationDestination010,
  SolaceOperationDestination020 => InternalSolaceOperationDestination020
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class SolaceOperationBinding(override private[amf] val _internal: InternalSolaceOperationBinding)
    extends OperationBinding
    with BindingVersion {
  @JSExportTopLevel("SolaceOperationBinding")
  def this() = this(InternalSolaceOperationBinding())

  def destinations: ClientList[SolaceOperationDestination] = _internal.destinations.asClient

  def withDestinations(destinations: ClientList[SolaceOperationDestination]): this.type = {
    _internal.withDestinations(destinations.asInternal)
    this
  }

  override protected def bindingVersion: StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): SolaceOperationBinding = _internal.linkCopy()
}

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

  def queue: SolaceOperationQueue = _internal.queue

  def withQueue(queue: SolaceOperationQueue): this.type = {
    _internal.withQueue(queue)
    this
  }

}

@JSExportAll
case class SolaceOperationDestination010(override private[amf] val _internal: InternalSolaceOperationDestination010)
    extends SolaceOperationDestination(_internal) {
  @JSExportTopLevel("SolaceOperationDestination010")
  def this() = this(InternalSolaceOperationDestination010())

}

@JSExportAll
case class SolaceOperationDestination020(override private[amf] val _internal: InternalSolaceOperationDestination020)
    extends SolaceOperationDestination(_internal) {
  @JSExportTopLevel("SolaceOperationDestination020")
  def this() = this(InternalSolaceOperationDestination020())
  def topic: SolaceOperationTopic = _internal.topic
  def withTopic(topic: SolaceOperationTopic): this.type = {
    _internal.withTopic(topic)
    this
  }
}
@JSExportAll
case class SolaceOperationQueue(override private[amf] val _internal: InternalSolaceOperationQueue)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("SolaceOperationQueue")
  def this() = this(InternalSolaceOperationQueue())

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
