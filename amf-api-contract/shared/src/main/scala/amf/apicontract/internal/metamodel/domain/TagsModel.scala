package amf.apicontract.internal.metamodel.domain

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait TagsModel {
  val Tags = Field(Array(TagModel),
                   ApiContract + "tag",
                   ModelDoc(ModelVocabularies.ApiContract, "tag", "Additionally custom tagged information"))
}
