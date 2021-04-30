package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Core
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel

/**
  * Documentation field.
  */
trait DocumentationField {
  val Documentation = Field(
    CreativeWorkModel,
    Core + "documentation",
    ModelDoc(ModelVocabularies.Core, "documentation", "Documentation for a particular part of the model")
  )
}

object DocumentationField extends DocumentationField
