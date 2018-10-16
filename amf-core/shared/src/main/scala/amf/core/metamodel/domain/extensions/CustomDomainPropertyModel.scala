package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.ValueType

/**
  * Custom Domain Property
  *
  * Definition of an extension to the domain model defined directly by a user in the RAML/OpenAPI document.
  *
  * This can be achieved by using an annotationType in RAML. In OpenAPI thy don't need to
  * be declared, they can just be used.
  *
  * This should be mapped to new RDF properties declared directly in the main document or module.
  *
  * Contrast this extension mechanism with the creation of a propertyTerm in a vocabulary, a more
  * re-usable and generic way of achieving the same functionality
  */
object CustomDomainPropertyModel extends DomainElementModel with KeyField with DisplayNameField with DescriptionField {

  /**
    * The name of the extension
    */
  val Name = Field(Str, Document + "name", ModelDoc(ModelVocabularies.AmlDoc, "name", "name for an entity"))

  override val key: Field = Name

  /**
    * These Iris are always going to be domain classes URIs.
    * No any class can be added to the domain.
    *
    * They are mapped from the allowedTargets property of an annotationType in RAML.
    */
  val Domain = Field(Array(Iri), Rdfs + "domain", ModelDoc(ExternalModelVocabularies.Rdfs, "domain", "RDFS domain property"))

  /**
    * A shape constraining the shape of the valid RDF graph for the property.
    * It is parsed from the RAML type associated to the annotationType.
    */
  val Schema = Field(ShapeModel, Shapes + "schema", ModelDoc(ModelVocabularies.Shapes, "schema", "Schema for an entity"))

  override def fields: List[Field] =
    List(Domain, Schema, Name) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "DomainProperty" :: Rdf + "Property" :: DomainElementModel.`type`

  override def modelInstance = CustomDomainProperty()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Custom Domain Property",
    "Definition of an extension to the domain model defined directly by a user in the RAML/OpenAPI document.\nThis can be achieved by using an annotationType in RAML. In OpenAPI thy don't need to\n      be declared, they can just be used.\n      This should be mapped to new RDF properties declared directly in the main document or module.\n      Contrast this extension mechanism with the creation of a propertyTerm in a vocabulary, a more\nre-usable and generic way of achieving the same functionality.\nIt can be validated using a SHACL shape"
  )
}
