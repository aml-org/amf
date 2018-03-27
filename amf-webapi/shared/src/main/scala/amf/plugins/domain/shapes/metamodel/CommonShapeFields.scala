package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.metamodel.Type.{Int, Str, Bool, Double}

trait CommonShapeFields {

  val Pattern = Field(Str, Shacl + "pattern")

  val MinLength = Field(Int, Shacl + "minLength")

  val MaxLength = Field(Int, Shacl + "maxLength")

  val Minimum = Field(Double, Shacl + "minInclusive")

  val Maximum = Field(Double, Shacl + "maxInclusive")

  val ExclusiveMinimum = Field(Bool, Shacl + "minExclusive")

  val ExclusiveMaximum = Field(Bool, Shacl + "maxExclusive")

  val Format = Field(Str, Shapes + "format")

  val MultipleOf = Field(Double, Shapes + "multipleOf")

  val commonOASFields =
    List(Pattern, MinLength, MaxLength, Minimum, Maximum, ExclusiveMinimum, ExclusiveMaximum, Format, MultipleOf)
}
