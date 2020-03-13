package amf.plugins.domain.webapi.models.bindings
import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{DataNode, DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.{DynamicBindingModel, EmptyBindingModel}
import amf.plugins.domain.webapi.metamodel.bindings.DynamicBindingModel._
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.models.Key

class DynamicBinding(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with ServerBinding
    with ChannelBinding
    with OperationBinding
    with MessageBinding
    with Key {
  override def meta: Obj = DynamicBindingModel

  def definition: DataNode = fields.field(Definition)
  def `type`: StrField     = fields.field(Type)

  def withDefinition(definition: DataNode): this.type = set(Definition, definition)
  def withType(`type`: String): this.type             = set(Type, `type`)

  override def componentId: String        = "/" + `type`.option().getOrElse("dynamic-binding").urlComponentEncoded
  override def linkCopy(): DynamicBinding = DynamicBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = DynamicBinding.apply

  override def key: StrField = fields.field(DynamicBindingModel.key)
}

object DynamicBinding {

  def apply(): DynamicBinding = apply(Annotations())

  def apply(annotations: Annotations): DynamicBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): DynamicBinding = new DynamicBinding(fields, annotations)
}

class EmptyBinding(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with ServerBinding
    with ChannelBinding
    with OperationBinding
    with MessageBinding
    with Key {

  override def meta: Obj = EmptyBindingModel

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
