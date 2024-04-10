package amf.apicontract.client.platform.model.domain.bindings.ibmmq

import amf.apicontract.client.platform.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.{IBMMQServerBinding => InternalIBMMQServerBinding}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model
import amf.core.client.platform.model.{BoolField, IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class IBMMQServerBinding(override private[amf] val _internal: InternalIBMMQServerBinding)
    extends ServerBinding
    with BindingVersion {
  @JSExportTopLevel("IBMMQServerBinding")
  def this() = this(InternalIBMMQServerBinding())

  def groupId: StrField              = _internal.groupId
  def ccdtQueueManagerName: StrField = _internal.ccdtQueueManagerName
  def cipherSpec: StrField           = _internal.cipherSpec
  def multiEndpointServer: BoolField = _internal.multiEndpointServer
  def heartBeatInterval: IntField    = _internal.heartBeatInterval

  def withGroupId(groupId: String): this.type = {
    _internal.withGroupId(groupId)
    this
  }

  def withCcdtQueueManagerName(ccdtQueueManagerName: String): this.type = {
    _internal.withCcdtQueueManagerName(ccdtQueueManagerName)
    this
  }

  def withCipherSpec(cipherSpec: String): this.type = {
    _internal.withCipherSpec(cipherSpec)
    this
  }

  def withMultiEndpointServer(multiEndpointServer: Boolean): this.type = {
    _internal.withMultiEndpointServer(multiEndpointServer)
    this
  }

  def withHeartBeatInterval(heartBeatInterval: Int): this.type = {
    _internal.withHeartBeatInterval(heartBeatInterval)
    this
  }

  override protected def bindingVersion: StrField = _internal.bindingVersion

  override def withBindingVersion(bindingVersion: String): this.type = {
    _internal.withBindingVersion(bindingVersion)
    this
  }

  override def linkCopy(): IBMMQServerBinding = _internal.linkCopy()
}
