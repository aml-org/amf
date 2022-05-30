package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait BindingType {

  val Type = Field(
    Str,
    ApiBinding + "type",
    ModelDoc(ModelVocabularies.ApiBinding, "type", "Binding for a corresponding known type")
  )

}

object BindingType extends BindingType
