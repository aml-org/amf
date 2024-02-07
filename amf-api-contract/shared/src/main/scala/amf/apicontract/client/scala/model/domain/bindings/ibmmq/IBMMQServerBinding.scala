package amf.apicontract.client.scala.model.domain.bindings.ibmmq

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ServerBinding}
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQServerBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQServerBindingModel._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.IBMMQ
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class IBMMQServerBinding(override val fields: Fields, override val annotations: Annotations)
    extends ServerBinding
    with BindingVersion
    with Key {

  override protected def bindingVersionField: Field = BindingVersion
  override def meta: IBMMQServerBindingModel.type   = IBMMQServerBindingModel

  def groupId: StrField              = fields.field(GroupId)
  def ccdtQueueManagerName: StrField = fields.field(CcdtQueueManagerName)
  def cipherSpec: StrField           = fields.field(CipherSpec)
  def multiEndpointServer: BoolField = fields.field(MultiEndpointServer)
  def heartBeatInterval: IntField    = fields.field(HeartBeatInterval)

  def withGroupId(groupId: String): this.type = set(GroupId, groupId)
  def withCcdtQueueManagerName(ccdtQueueManagerName: String): this.type =
    set(CcdtQueueManagerName, ccdtQueueManagerName)
  def withCipherSpec(cipherSpec: String): this.type                    = set(CipherSpec, cipherSpec)
  def withMultiEndpointServer(multiEndpointServer: Boolean): this.type = set(MultiEndpointServer, multiEndpointServer)
  def withHeartBeatInterval(heartBeatInterval: Int): this.type         = set(HeartBeatInterval, heartBeatInterval)

  override def key: StrField = fields.field(IBMMQServerBindingModel.key)

  override def componentId: String = s"/$IBMMQ-server"

  override def linkCopy(): IBMMQServerBinding = IBMMQServerBinding()

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    IBMMQServerBinding.apply
}

object IBMMQServerBinding {

  def apply(): IBMMQServerBinding = apply(Annotations())

  def apply(annotations: Annotations): IBMMQServerBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): IBMMQServerBinding =
    new IBMMQServerBinding(fields, annotations)
}
