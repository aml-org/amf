package amf.apicontract.client.scala.model.domain.bindings

import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.bindings.MessageBindingsModel
import amf.apicontract.internal.metamodel.domain.bindings.MessageBindingsModel.{Bindings, Name}
import org.yaml.model.YMap
import amf.core.internal.utils.AmfStrings

case class MessageBindings(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def bindings: Seq[MessageBinding]                          = fields.field(Bindings)
  def withBindings(bindings: Seq[MessageBinding]): this.type = setArray(Bindings, bindings)

  override def meta: MessageBindingsModel.type = MessageBindingsModel

  override def nameField: Field = Name

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/" + name.option().getOrElse("message-bindings").urlComponentEncoded

  override def linkCopy(): MessageBindings = {
    val bindings = MessageBindings().withId(id)
    name.option().foreach(bindings.withName(_))
    bindings
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new MessageBindings(fields, annot)
}

object MessageBindings {

  def apply(): MessageBindings = apply(Annotations())

  def apply(ast: YMap): MessageBindings = apply(Annotations(ast))

  def apply(annotations: Annotations): MessageBindings = new MessageBindings(Fields(), annotations)
}
