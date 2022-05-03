package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}

trait BindingHeaders {
  val Headers = Field(
    ShapeModel,
    ApiBinding + "headers",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "headers",
      "A Schema object containing the definitions for HTTP-specific headers"
    )
  )
}

object BindingHeaders extends BindingHeaders
