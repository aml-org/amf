package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Schema
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel

/**
  * Documentation field.
  */
trait DocumentationField {
  val Documentation = Field(CreativeWorkModel, Schema + "documentation", ModelDoc(ExternalModelVocabularies.SchemaOrg, "documentation", "Documentation for a particular part of the model"))
}

object DocumentationField extends DocumentationField
