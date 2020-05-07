package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.ApiBinding

trait BindingHeaders {
  val Headers = Field(
    ShapeModel,
    ApiBinding + "headers",
    ModelDoc(ModelVocabularies.ApiBinding,
             "headers",
             "A Schema object containing the definitions for HTTP-specific headers")
  )
}

object BindingHeaders extends BindingHeaders
