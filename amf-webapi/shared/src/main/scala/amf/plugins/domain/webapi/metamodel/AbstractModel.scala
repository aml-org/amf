package amf.plugins.domain.webapi.metamodel
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Bool
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract

trait AbstractModel {
  val IsAbstract = Field(Bool,
                         ApiContract + "isAbstract",
                         ModelDoc(ModelVocabularies.ApiContract, "isAbstract", "Defines a model as abstract"))
}
