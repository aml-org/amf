package amf.plugins.domain.apicontract.models.bindings
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.BindingType.Type
import amf.plugins.domain.apicontract.metamodel.bindings.EmptyBindingModel
import amf.plugins.domain.apicontract.models.Key
import amf.core.internal.utils.AmfStrings

class EmptyBinding(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with ServerBinding
    with ChannelBinding
    with OperationBinding
    with MessageBinding
    with Key {

  override def meta: EmptyBindingModel.type = EmptyBindingModel

  def `type`: StrField = fields.field(Type)

  override def key: StrField = fields.field(EmptyBindingModel.key)

  def withType(`type`: String): this.type = set(Type, `type`)

  override def componentId: String      = "/" + `type`.option().getOrElse("empty-binding").urlComponentEncoded
  override def linkCopy(): EmptyBinding = EmptyBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = EmptyBinding.apply
}

object EmptyBinding {

  def apply(): EmptyBinding = apply(Annotations())

  def apply(annotations: Annotations): EmptyBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): EmptyBinding = new EmptyBinding(fields, annotations)
}
