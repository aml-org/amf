package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField

trait AbstractParameterModel
    extends DomainElementModel
    with LinkableElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField {

  val ParameterName: Field = Field(
    Str,
    Core + "paramName",
    ModelDoc(ModelVocabularies.ApiContract, "paramName", "Name of a parameter", Seq((Namespace.Core + "name").iri()))
  )

  val Binding: Field = Field(
    Str,
    Core + "binding",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "binding",
      "Part of the Request model where the parameter can be encoded (header, path, query param, etc.)"
    )
  )

  val Required: Field =
    Field(
      Bool,
      Core + "required",
      ModelDoc(ModelVocabularies.ApiContract, "required", "Marks the parameter as required")
    )

  val Schema: Field = Field(
    ShapeModel,
    Shapes + "schema",
    ModelDoc(ModelVocabularies.Shapes, "schema", "Schema the parameter value must validate")
  )

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(
      Name,
      ParameterName,
      Binding,
      Description,
      Required,
      Schema
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "AbstractParameter",
    "Piece of data required or returned by an Operation"
  )
}

object AbstractParameterModel extends AbstractParameterModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractParameterModel is an abstract class")
}
