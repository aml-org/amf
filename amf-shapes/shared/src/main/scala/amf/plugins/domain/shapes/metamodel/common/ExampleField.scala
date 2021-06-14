package amf.plugins.domain.shapes.metamodel.common

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type._
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
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
