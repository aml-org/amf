package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.Shape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{ONE_OF_FIELD, ONE_OF_NAME}
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}
import org.mulesoft.antlrast.ast.Node

import scala.collection.mutable

case class GrpcOneOfParser(ast: Node)(implicit context: GrpcWebApiContext) extends GrpcASTParserHelper {

  val union: UnionShape = UnionShape(toAnnotations(ast))

  def parse(adopt: UnionShape => Unit): UnionShape = {
    parseName(adopt)
    parseMembers()
    union
  }

  protected def parseMembers(): Unit = {
    val members: mutable.Buffer[Shape] = mutable.Buffer()
    collect(ast, Seq(ONE_OF_FIELD)).map { case (oneOfField: Node) =>
      GrpcFieldParser(oneOfField).parse(property => {
        val shape = NodeShape(toAnnotations(oneOfField)).adopted(union.id)
        shape.withProperties(Seq(property.adopted(shape.id)))
        members.append(shape)
      })
    }
    union.withAnyOf(members)
  }

  protected def parseName(adopt: UnionShape => Unit): Unit = withName(ast, ONE_OF_NAME, union, { _ => adopt(union) })
}
