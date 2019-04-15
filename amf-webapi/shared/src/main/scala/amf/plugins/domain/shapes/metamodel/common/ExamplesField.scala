package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.Document
import amf.plugins.domain.shapes.metamodel.ExamplesModel

trait ExamplesField {
  val Examples = Field(ExamplesModel,
                       Document + "examples",
                       ModelDoc(ModelVocabularies.Http, "examples", "Examples for a particular domain element"))
}

object ExamplesField extends ExamplesField
