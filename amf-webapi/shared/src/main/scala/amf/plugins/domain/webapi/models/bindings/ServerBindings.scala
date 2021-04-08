package amf.plugins.domain.webapi.models.bindings

import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.bindings.ServerBindingsModel
import amf.plugins.domain.webapi.metamodel.bindings.ServerBindingsModel.{Bindings, Name}
import org.yaml.model.YMap
import amf.core.utils.AmfStrings

case class ServerBindings(fields: Fields, annotations: Annotations) extends NamedDomainElement with Linkable {

  def bindings: Seq[ServerBinding]                          = fields.field(Bindings)
  def withBindings(bindings: Seq[ServerBinding]): this.type = setArray(Bindings, bindings)

  override def meta: ServerBindingsModel.type = ServerBindingsModel

  override def nameField: Field = Name

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String =
    "/" + name.option().getOrElse("server-bindings").urlComponentEncoded

  override def linkCopy(): ServerBindings = {
    val bindings = ServerBindings().withId(id)
    name.option().foreach(bindings.withName(_))
    bindings
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    (fields, annot) => new ServerBindings(fields, annot)
}

object ServerBindings {

  def apply(): ServerBindings = apply(Annotations())

  def apply(ast: YMap): ServerBindings = apply(Annotations(ast))

  def apply(annotations: Annotations): ServerBindings = new ServerBindings(Fields(), annotations)
}
