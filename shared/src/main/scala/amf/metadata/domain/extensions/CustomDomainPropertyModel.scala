package amf.metadata.domain.extensions

import amf.metadata.Field
import amf.metadata.Type.{Array, Iri, Str}
import amf.vocabulary.Namespace.{Rdfs, Rdf, Shapes, Document}
import amf.metadata.domain.DomainElementModel
import amf.metadata.shape.ShapeModel
import amf.vocabulary.ValueType

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
object CustomDomainPropertyModel extends DomainElementModel {

  /**
    * The name of the extension
    */
  val Name        = Field(Str, amf.vocabulary.Namespace.Document + "name")
  val DisplayName = Field(Str, amf.vocabulary.Namespace.Schema + "name")
  val Description = Field(Str, amf.vocabulary.Namespace.Schema + "description")

  /**
    * These Iris are always going to be domain classes URIs.
    * No any class can be added to the domain.
    *
    * They are mapped from the allowedTargets property of an annotationType in RAML.
    */
  val Domain = Field(Array(Iri), Rdfs + "domain")

  /**
    * A shape constraining the shape of the valid RDF graph for the property.
    * It is parsed from the RAML type associated to the annotationType.
    */
  val Schema = Field(ShapeModel, Shapes + "schema")

  override val fields: List[Field] = List(Domain, Schema, Name) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Rdf + "Property" :: Document + "DomainProperty" :: DomainElementModel.`type`
}
