package amf.apicontract.internal.metamodel.domain

import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Bool
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait AbstractModel {
  val IsAbstract = Field(Bool,
                         ApiContract + "isAbstract",
                         ModelDoc(ModelVocabularies.ApiContract, "isAbstract", "Defines a model as abstract"))
}

object AbstractModel extends AbstractModel
