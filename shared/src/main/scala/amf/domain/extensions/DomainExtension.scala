package amf.domain.extensions

import amf.common.AMFAST
import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.extensions.DomainExtensionModel.{DefinedBy, Extension}

case class DomainExtension(fields: Fields, annotations: Annotations) extends DomainElement {


  def definedBy: CustomDomainProperty = fields(DefinedBy)
  def extension: DataNode             = fields(Extension)

  def withDefinedBy(customProperty: CustomDomainProperty): this.type =
    set(DefinedBy, customProperty)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String) = withId(parent + "/extension" )

}

object DomainExtension {
  def apply(): DomainExtension = apply(Annotations())

  def apply(ast: AMFAST): DomainExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): DomainExtension = DomainExtension(Fields(), annotations)
}

