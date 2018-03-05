package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel.{DefinedBy, Extension, Element, Name}
import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class DomainExtension(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String = fields(Name)

  def definedBy: CustomDomainProperty = fields(DefinedBy)

  def extension: DataNode = fields(Extension)

  def element: String = fields(Element)

  def withDefinedBy(customProperty: CustomDomainProperty): this.type = set(DefinedBy, customProperty)

  def withName(name: String): this.type = set(Name, name)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  def withElement(element: String): this.type = set(Element, element)

  def isScalarExtension: Boolean = fields.?[String](Element).isDefined

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String): this.type = withId(parent + "/extension")

  def meta: DomainExtensionModel = DomainExtensionModel
}

object DomainExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: YPart): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = new DomainExtension(Fields(), annotations)
}
