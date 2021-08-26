package amf.shapes.internal.domain.metamodel.core

import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Shapes}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{
  DomainElementModel,
  LinkableElementModel,
  ModelDoc,
  ModelVocabularies,
  ShapeModel
}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Str}
import amf.shapes.client.scala.model.domain.core.ShapeParameter

object ShapeParameterModel
    extends DomainElementModel
    with LinkableElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField {

  val ParameterName = Field(
    Str,
    ApiContract + "paramName",
    ModelDoc(ModelVocabularies.ApiContract, "paramName", "Name of a parameter", Seq((Namespace.Core + "name").iri())))

  val Required =
    Field(Bool,
          ApiContract + "required",
          ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required"))

  val Schema = Field(ShapeModel,
                     Shapes + "schema",
                     ModelDoc(ModelVocabularies.Shapes, "schema", "Schema the parameter value must validate"))

  override val key: Field = Name

  override val `type`: List[ValueType] = ApiContract + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(Name, ParameterName, Description, Required, Schema) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = ShapeParameter()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Parameter",
    "Piece of data required or returned by an Operation"
  )
}
