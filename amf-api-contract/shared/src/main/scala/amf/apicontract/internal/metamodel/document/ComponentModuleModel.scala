package amf.apicontract.internal.metamodel.document

import amf.apicontract.client.scala.model.document.ComponentModule
import amf.apicontract.internal.metamodel.domain.common.VersionField
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.{DocumentModel, ModuleModel}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

/**
  * A component module is a specialization of module that introduce some extra fields
  */
object ComponentModuleModel extends ModuleModel with VersionField with NameFieldSchema {
  override val `type`: List[ValueType] = List(ApiContract + "ComponentModule") ++ DocumentModel.`type`

  override def modelInstance = ComponentModule()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ComponentModule",
    "Model defining a Component Module"
  )

  override def fields: List[Field] = ModuleModel.fields ++ List(Name, Version)
}
