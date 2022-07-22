package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Str}
import amf.core.internal.metamodel.domain.federation.HasShapeFederationMetadataModel
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapeParameter

object ShapeParameterModel extends AbstractParameterModel with HasShapeFederationMetadataModel {

  override val ParameterName: Field = Field(
    Str,
    Shapes + "paramName",
    ModelDoc(ModelVocabularies.Shapes, "paramName", "Name of a parameter", Seq((Namespace.Shapes + "name").iri()))
  )

  override val Binding: Field = Field(
    Str,
    Shapes + "binding",
    ModelDoc(
      ModelVocabularies.Shapes,
      "binding",
      "Part of the Request model where the parameter can be encoded (header, path, query param, etc.)"
    )
  )

  override val Required: Field =
    Field(Bool, Shapes + "required", ModelDoc(ModelVocabularies.Shapes, "required", "Marks the parameter as required"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Parameter" :: Core + "Parameter" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(
      Name,
      ParameterName,
      Binding,
      Description,
      Required,
      Schema,
      FederationMetadata,
      Default
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ShapeParameter",
    "Piece of data required or returned by an Operation"
  )

  override def modelInstance: ShapeParameter = ShapeParameter()

}
