package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.plugins.domain.shapes.metamodel.ExampleModel

/**
  * Example field.
  */
trait ExampleField {
  val Examples = Field(Array(ExampleModel),
                       Http + "example",
                       ModelDoc(ModelVocabularies.Http, "example", "Single example for a particular domain element"))
}

trait ExamplesField {
  val Examples = Field(Array(ExampleModel),
                       Document + "examples",
                       ModelDoc(ModelVocabularies.Http, "examples", "Examples for a particular domain element"))
}

object ExampleField extends ExampleField

//object ExampleFieldDocument extends ExampleFieldDocument

object ExamplesField extends ExamplesField
