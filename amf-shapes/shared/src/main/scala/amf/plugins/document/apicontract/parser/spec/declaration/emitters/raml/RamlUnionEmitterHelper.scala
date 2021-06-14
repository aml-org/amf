package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.scala.model.domain.Shape
import amf.plugins.document.apicontract.parser.RamlTypeDefStringValueMatcher
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping

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
