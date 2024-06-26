package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.{
  AnyShape,
  ArrayShape,
  NilShape,
  NodeShape,
  ScalarShape,
  ShapeHelpers,
  UnionShape
}
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import amf.shapes.internal.domain.parser.TypeDefXsdMapping
import amf.shapes.internal.spec.RamlTypeDefStringValueMatcher

object RamlUnionEmitterHelper {
  def inlinedEmission(shape: UnionShape with ShapeHelpers): Option[String] = {
    shape.typeExpression // use union expression stored in annotation
      .orElse {
        val unionTypes: Seq[Option[String]] = shape.anyOf.map(shapeAsSingleType)
        if (unionTypes.forall(_.isDefined)) Some(unionTypes.flatten.mkString(" | "))
        else None

      }
  }

  def shapeAsSingleType(s: Shape): Option[String] = s match {
    case scalar: ScalarShape if isSimpleScalar(scalar) =>
      val typeName = RamlTypeDefStringValueMatcher
        .matchType(TypeDefXsdMapping.typeDef(scalar.dataType.value()), scalar.format.option())
      Some(typeName.format.getOrElse(typeName.typeDef))
    case s: Shape if s.isLink && s.linkLabel.option().isDefined => Some(s.linkLabel.value())
    case n: NilShape if n.fields.fields().isEmpty               => Some("nil")
    case a: ArrayShape if a.fields.fields().isEmpty             => Some("array")
    case a: NodeShape if a.fields.fields().isEmpty              => Some("object")
    case a: AnyShape if a.fields.fields().isEmpty               => Some("any")
    case _                                                      => None
  }

  private def isSimpleScalar(scalar: ScalarShape): Boolean =
    scalar.fields.fields().size <= 2 && scalar.fields
      .fields()
      .map(_.field)
      .forall(f => f == ScalarShapeModel.Name || f == ScalarShapeModel.DataType)
}
