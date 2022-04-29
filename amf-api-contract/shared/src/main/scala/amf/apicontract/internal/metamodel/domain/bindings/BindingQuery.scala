package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}

trait BindingQuery {
  val Query = Field(
    ShapeModel,
    ApiBinding + "query",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "query",
      "A Schema object containing the definitions for each query parameter"
    )
  )
}

object BindingQuery extends BindingQuery
