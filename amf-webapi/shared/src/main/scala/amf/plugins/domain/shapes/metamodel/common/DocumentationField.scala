package amf.plugins.domain.shapes.metamodel.common

import amf.core.metamodel.Field
import amf.core.vocabulary.Namespace.Schema
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel

/**
  * Documentation field.
  */
trait DocumentationField {
  val Documentation = Field(CreativeWorkModel, Schema + "documentation")
}

object DocumentationField extends DocumentationField
