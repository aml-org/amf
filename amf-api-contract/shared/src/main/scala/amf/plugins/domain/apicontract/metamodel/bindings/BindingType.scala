package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiBinding

trait BindingType {

  val Type = Field(Str,
                   ApiBinding + "type",
                   ModelDoc(ModelVocabularies.ApiBinding, "type", "Binding for a corresponding known type"))

}

object BindingType extends BindingType
