package amf.plugins.domain.webapi.metamodel
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract

trait TagsModel {
  val Tags = Field(Array(TagModel),
                   ApiContract + "tag",
                   ModelDoc(ModelVocabularies.ApiContract, "tag", "Additionally custom tagged information"))
}
