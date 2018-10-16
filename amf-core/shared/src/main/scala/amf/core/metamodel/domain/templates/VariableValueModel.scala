package amf.core.metamodel.domain.templates

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.templates.VariableValue
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

object VariableValueModel extends DomainElementModel {

  val Name = Field(Str, Document + "name", ModelDoc(ModelVocabularies.AmlDoc, "name", "name of the template variable"))

  val Value = Field(DataNodeModel, Document + "value", ModelDoc(ModelVocabularies.AmlDoc, "value", "value of the variables"))

  override def fields: List[Field] = List(Name, Value) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = Document + "VariableValue" :: DomainElementModel.`type`

  override def modelInstance = VariableValue()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Variable Value",
    "Value for a variable in a graph template"
  )
}
