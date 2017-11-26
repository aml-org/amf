package amf.core.model.domain.extensions

import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel._
import org.yaml.model.YPart

case class DomainExtension(fields: Fields, annotations: Annotations) extends DomainElement {

  id = "http://raml.org/vocabularies#document/domain_extension"

  def name: String                    = fields(Name)
  def definedBy: CustomDomainProperty = fields(DefinedBy)
  def extension: DataNode             = fields(Extension)

  def withDefinedBy(customProperty: CustomDomainProperty): this.type =
    set(DefinedBy, customProperty)

  def withName(name: String): this.type =
    set(Name, name)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String): this.type = withId(parent + "/extension")

  override def meta = DomainExtensionModel
}

object DomainExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: YPart): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = DomainExtension(Fields(), annotations)
}
