package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.templates.KeyField
import amf.core.model.domain.extensions.{DomainExtension, DomainScalarExtension}
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.core.vocabulary.ValueType

/**
  * Extension to the support scalar-value annotations from RAML. Includes an element field pointing to scalar property.
  */
object DomainScalarExtensionModel extends DomainExtensionModel with KeyField {

  val Element = Field(Str, Document + "element")

  override def fields: List[Field] = List(Element) ++ DomainExtensionModel.fields

  override val `type`: List[ValueType] = Http + "DomainScalarExtension" :: DomainExtensionModel.`type`

  override def modelInstance = DomainScalarExtension()
}
