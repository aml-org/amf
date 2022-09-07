package amf.apicontract.internal.validation.shacl.graphql.values

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{Annotation, DataNode, ScalarNode, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DataNodeModel, ScalarNodeModel}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{ArrayShape, NilShape, NodeShape, ScalarShape, UnionShape}
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

trait ValueValidator[S <: Shape] {
  protected val defaultField: Field = ScalarNodeModel.Value

  def validate(shape: S, value: DataNode): Seq[ValidationInfo]

  // helper
  protected def isNull(s: ScalarNode): Boolean = s.dataType.value() == DataType.Nil

  def typeError(expected: String, actual: String, annotations: Annotations): ValidationInfo = {
    val message = s"Expecting '$expected' value but got '$actual' value instead"
    ValidationInfo(defaultField, Some(message), Some(annotations))
  }
}

object ValueValidator {
  def validate(shape: Shape, value: DataNode): Seq[ValidationInfo] = {
    shape match {
      case _ if value == null                                        => Nil
      case s: ScalarShape if s.values.nonEmpty                       => EnumValueValidator.validate(s, value)
      case s: ScalarShape                                            => ScalarValueValidator.validate(s, value)
      case a: ArrayShape                                             => ListValueValidator.validate(a, value)
      case n: NodeShape                                              => ObjectValueValidator.validate(n, value)
      case u: UnionShape if u.anyOf.exists(_.isInstanceOf[NilShape]) => NullableValueValidator.validate(u, value)
      case _                                                         => Nil
    }
  }
}
