package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel.{DefinedBy, Element, Extension, Name}
import amf.core.model.StrField
import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class DomainExtension(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                  = fields.field(Name)
  def definedBy: CustomDomainProperty = fields.field(DefinedBy)
  def extension: DataNode             = fields.field(Extension)
  def element: StrField               = fields.field(Element)

  def withDefinedBy(customProperty: CustomDomainProperty): this.type = set(DefinedBy, customProperty)
  def withName(name: String): this.type                              = set(Name, name)
  def withExtension(extension: DataNode): this.type                  = set(Extension, extension)
  def withElement(element: String): this.type                        = set(Element, element)

  def isScalarExtension: Boolean = !element.isNullOrEmpty

  def meta: DomainExtensionModel = DomainExtensionModel

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/extension"

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): this.type = {
    if (Option(id) == None || id.startsWith("null/")) simpleAdoption(parent)
    this
  }

}

object DomainExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: YPart): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = new DomainExtension(Fields(), annotations)
}
