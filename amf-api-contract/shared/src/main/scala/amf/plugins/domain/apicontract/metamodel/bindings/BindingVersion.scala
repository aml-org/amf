package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

trait BindingVersion {
  val BindingVersion = Field(Str,
                             ApiBinding + "bindingVersion",
                             ModelDoc(ModelVocabularies.ApiBinding, "bindingVersion", "The version of this binding"))
}

object BindingVersion extends BindingVersion
