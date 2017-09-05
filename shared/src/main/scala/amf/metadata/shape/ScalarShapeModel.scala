package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Int, Iri, Str}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Shacl, Shapes}
import amf.vocabulary.ValueType

/**
  * Scalar shape metamodel
  */
object ScalarShapeModel extends ShapeModel with DomainElementModel {

  val DataType = Field(Iri, Shacl + "datatype")

  val Pattern = Field(Str, Shacl + "pattern")

  val MinLength = Field(Int, Shacl + "minLength")

  val MaxLength = Field(Int, Shacl + "maxLength")

  val Minimum = Field(Str, Shacl + "minInclusive")

  val Maximum = Field(Str, Shacl + "maxInclusive")

  val ExclusiveMinimum = Field(Str, Shacl + "minExclusive")

  val ExclusiveMaximum = Field(Str, Shacl + "maxExclusive")

  val Format = Field(Str, Shapes + "format")

  val MultipleOf = Field(Int, Shapes + "multipleOf")

  override val fields: List[Field] = List(DataType,
                                          Pattern,
                                          MinLength,
                                          MaxLength,
                                          Minimum,
                                          Maximum,
                                          ExclusiveMinimum,
                                          ExclusiveMaximum,
                                          Format,
                                          MultipleOf) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "ScalarShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}
