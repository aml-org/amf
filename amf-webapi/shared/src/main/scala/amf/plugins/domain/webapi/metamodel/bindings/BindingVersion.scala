package amf.plugins.domain.webapi.metamodel.bindings
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract

trait BindingVersion {
  val BindingVersion = Field(Str,
                             ApiContract + "bindingVersion",
                             ModelDoc(ModelVocabularies.ApiContract, "bindingVersion", "The version of this binding"))
}
