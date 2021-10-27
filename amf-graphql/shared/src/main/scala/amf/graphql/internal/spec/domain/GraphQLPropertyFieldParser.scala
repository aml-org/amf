package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLPropertyFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {
  val property = PropertyShape(toAnnotations(ast))

  def parse(adopt: PropertyShape => Unit): PropertyShape = {
    parseName()
    adopt(property)
    parseDescription()
    parseRange()
    property
  }

  private def parseName(): Unit = {
    property.withName(findName(ast, "AnonymousField", "", "Missing name for field"))
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(t => cleanDocumentation(t.value)).foreach(property.withDescription)
  }

  private def parseRange(): Unit = {
    val range = parseType(ast, property.id)
    unpackNilUnion(range) match {
      case NullableShape(true, shape)  => property.withRange(shape).withMinCount(0)
      case NullableShape(false,shape) => property.withRange(shape).withMinCount(1)
    }
  }
}