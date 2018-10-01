package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.vocabulary.Namespace.{Document, Http}
import amf.plugins.domain.shapes.metamodel.ExampleModel

/**
  * Example field.
  */
trait ExampleField {
  val Examples = Field(Array(ExampleModel), Http + "example")
}

trait ExamplesField {
  val Examples = Field(Array(ExampleModel), Document + "examples")
}

object ExampleField extends ExampleField

//object ExampleFieldDocument extends ExampleFieldDocument

object ExamplesField extends ExamplesField