package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract
import amf.plugins.domain.shapes.metamodel.ExampleModel

/**
  * Example field.
  */
trait ExamplesField {
  val Examples = Field(Array(ExampleModel),
                       ApiContract + "examples",
                       ModelDoc(ModelVocabularies.ApiContract, "examples", "Examples for a particular domain element"))
}

//object ExampleFieldDocument extends ExampleFieldDocument

object ExamplesField extends ExamplesField
