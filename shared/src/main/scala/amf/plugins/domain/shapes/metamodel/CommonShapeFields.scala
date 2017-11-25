package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Int, Str}
import amf.core.vocabulary.Namespace.{Shacl, Shapes}

trait CommonShapeFields {

  val Pattern = Field(Str, Shacl + "pattern")

  val MinLength = Field(Int, Shacl + "minLength")

  val MaxLength = Field(Int, Shacl + "maxLength")

  val Minimum = Field(Int, Shacl + "minInclusive")

  val Maximum = Field(Int, Shacl + "maxInclusive")

  val ExclusiveMinimum = Field(Str, Shacl + "minExclusive")

  val ExclusiveMaximum = Field(Str, Shacl + "maxExclusive")

  val Format = Field(Str, Shapes + "format")

  val MultipleOf = Field(Int, Shapes + "multipleOf")

  val commonOASFields =
    List(Pattern, MinLength, MaxLength, Minimum, Maximum, ExclusiveMinimum, ExclusiveMaximum, Format, MultipleOf)
}
