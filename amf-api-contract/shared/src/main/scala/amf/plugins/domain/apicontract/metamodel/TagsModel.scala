package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.Type.Array

trait TagsModel {
  val Tags = Field(Array(TagModel),
                   ApiContract + "tag",
                   ModelDoc(ModelVocabularies.ApiContract, "tag", "Additionally custom tagged information"))
}
