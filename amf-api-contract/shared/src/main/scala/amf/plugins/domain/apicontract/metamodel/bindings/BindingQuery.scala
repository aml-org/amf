package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.ApiBinding

trait BindingQuery {
  val Query = Field(ShapeModel,
                    ApiBinding + "query",
                    ModelDoc(ModelVocabularies.ApiBinding,
                             "query",
                             "A Schema object containing the definitions for each query parameter"))
}

object BindingQuery extends BindingQuery
