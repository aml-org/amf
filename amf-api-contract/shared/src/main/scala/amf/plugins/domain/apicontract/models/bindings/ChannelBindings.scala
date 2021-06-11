package amf.plugins.domain.apicontract.models.bindings

import amf.core.metamodel.{Field, Obj}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.bindings.ChannelBindingsModel
import amf.plugins.domain.apicontract.metamodel.bindings.ChannelBindingsModel.{Bindings, Name}
import org.yaml.model.YMap
import amf.core.utils.AmfStrings

case class ChannelBindings(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def bindings: Seq[ChannelBinding]                          = fields.field(Bindings)
  def withBindings(bindings: Seq[ChannelBinding]): this.type = setArray(Bindings, bindings)

  override def meta: ChannelBindingsModel.type = ChannelBindingsModel

  override def nameField: Field = Name

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + name.option().getOrElse("channel-bindings").urlComponentEncoded

  override def linkCopy(): ChannelBindings = {
    val bindings = ChannelBindings().withId(id)
    name.option().foreach(bindings.withName(_))
    bindings
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new ChannelBindings(fields, annot)
}

object ChannelBindings {

  def apply(): ChannelBindings = apply(Annotations())

  def apply(ast: YMap): ChannelBindings = apply(Annotations(ast))

  def apply(annotations: Annotations): ChannelBindings = new ChannelBindings(Fields(), annotations)
}
