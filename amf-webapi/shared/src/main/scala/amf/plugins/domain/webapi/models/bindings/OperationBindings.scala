package amf.plugins.domain.webapi.models.bindings

import amf.core.metamodel.Field
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.metamodel.bindings.OperationBindingsModel
import amf.plugins.domain.webapi.metamodel.bindings.OperationBindingsModel.{Bindings, Name}
import org.yaml.model.YMap

case class OperationBindings(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def bindings: Seq[OperationBinding]                          = fields.field(Bindings)
  def withBindings(bindings: Seq[OperationBinding]): this.type = setArray(Bindings, bindings)

  override def meta: OperationBindingsModel.type = OperationBindingsModel

  override def nameField: Field = Name

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + name.option().getOrElse("operation-bindings").urlComponentEncoded

  override def linkCopy(): OperationBindings = {
    val bindings = OperationBindings().withId(id)
    name.option().foreach(bindings.withName(_))
    bindings
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new OperationBindings(fields, annot)
}

object OperationBindings {

  def apply(): OperationBindings = apply(Annotations())

  def apply(ast: YMap): OperationBindings = apply(Annotations(ast))

  def apply(annotations: Annotations): OperationBindings = new OperationBindings(Fields(), annotations)
}
