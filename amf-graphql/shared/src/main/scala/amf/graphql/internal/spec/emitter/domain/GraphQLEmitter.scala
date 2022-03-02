package amf.graphql.internal.spec.emitter.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.parser.syntax.NullableShape
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.plugins.parse.GraphQLParsePlugin._
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, ScalarShape, UnionShape}
import org.mulesoft.common.client.lexical.Position

trait GraphQLEmitter {

  def cleanNonNullable(name: String): String = if (name.endsWith("!")) name.dropRight(1) else name

  def typeTarget(shape: Shape): String = {
    shape.linkLabel.option() match {
      case Some(target) => target + "!"
      case _ =>
        shape match {
          case sc: ScalarShape =>
            sc.dataType.value() match {
              case s if s == DataType.Integer                                     => INT + "!"
              case s if s == DataType.Float                                       => FLOAT + "!"
              case s if s == DataType.Boolean                                     => BOOLEAN + "!"
              case s if s == DataType.String && sc.format.option().contains("ID") => ID + "!"
              case _                                                              => STRING + "!"
            }
          case l: ArrayShape => s"[${typeTarget(l.items.asInstanceOf[AnyShape])}]!"
          case u: UnionShape =>
            unpackNilUnion(u) match {
              case NullableShape(false, s) => s"${u.name.value()}!"
              case NullableShape(true, s)  => s"${cleanNonNullable(typeTarget(s))}"
            }
          case _ =>
            throw new Exception(s"Type of target $shape not supported yet")
        }
    }
  }

  def documentationEmitter(doc: String, b: StringDocBuilder, pos: Option[Position] = None): StringDocBuilder = {
    b.fixed { l =>
      if (pos.isDefined) {
        l.+=("\"\"\"", pos.get)
      } else {
        l.+=("\"\"\"")
      }
      l.+=(doc)
      l.+=("\"\"\"")
    }
  }
}
