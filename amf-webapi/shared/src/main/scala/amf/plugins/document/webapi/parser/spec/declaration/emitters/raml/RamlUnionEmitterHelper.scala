package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.RamlTypeDefStringValueMatcher
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping

object RamlUnionEmitterHelper {
  def inlinedEmission(shape: UnionShape): Option[String] = {
    val union: Seq[String] = shape.anyOf.map {
      case scalar: ScalarShape if isSimpleScalar(scalar) =>
        RamlTypeDefStringValueMatcher
          .matchType(TypeDefXsdMapping.typeDef(scalar.dataType.value()), scalar.format.option())
          ._1
      case s: Shape if s.isLink && s.linkLabel.option().isDefined => s.linkLabel.value()
      case n: NilShape if n.fields.fields().isEmpty               => "nil"
      case a: ArrayShape if a.fields.fields().isEmpty             => "array"
      case a: NodeShape if a.fields.fields().isEmpty              => "object"
      case a: AnyShape if a.fields.fields().isEmpty               => "any"
      case _                                                      => return None
    }
    Some(union.mkString(" | "))
  }

  private def isSimpleScalar(scalar: ScalarShape): Boolean =
    scalar.fields.fields().size <= 2 && scalar.fields
      .fields()
      .map(_.field)
      .forall(f => f == ScalarShapeModel.Name || f == ScalarShapeModel.DataType)
}
