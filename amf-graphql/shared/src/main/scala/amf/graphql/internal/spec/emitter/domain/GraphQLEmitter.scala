package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.internal.validation.shacl.graphql.GraphQLDataTypes
import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.parser.syntax.NullableShape
import amf.graphql.internal.spec.plugins.parse.GraphQLParsePlugin._
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape, UnionShape}

trait GraphQLEmitter {

  def cleanNonNullable(name: String): String = if (name.endsWith("!")) name.dropRight(1) else name

  def typeTarget(shape: Shape): String = {
    shape.linkLabel.option() match {
      case Some(target) => target + "!"
      case _ =>
        shape match {
          case sc: ScalarShape => s"${GraphQLDataTypes.from(sc)}!"
          case l: ArrayShape   => s"[${typeTarget(l.items.asInstanceOf[AnyShape])}]!"
          case u: UnionShape =>
            unpackNilUnion(u) match {
              case NullableShape(false, s) => s"${u.name.value()}!"
              case NullableShape(true, s)  => s"${cleanNonNullable(typeTarget(s))}"
            }
          case n: NodeShape => n.name.value()
          case _ =>
            throw new Exception(s"Type of target $shape not supported yet")
        }
    }
  }
}
