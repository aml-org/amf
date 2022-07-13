package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLPropertyFieldParser(ast: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val property: PropertyShape = PropertyShape(toAnnotations(ast))

  def parse(setterFn: PropertyShape => Unit): Unit = {
    parseName()
    setterFn(property)
    parseDescription()
    parseRange()
    GraphQLDirectiveApplicationParser(ast, property).parse()
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(ast, "AnonymousField", "Missing name for field")
    property.withName(name, annotations)
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(t => cleanDocumentation(t.value)).foreach(property.withDescription)
  }

  private def parseRange(): Unit = {
    val range = parseType(ast)
    unpackNilUnion(range) match {
      case NullableShape(true, shape)  => property.withRange(shape).withMinCount(0)
      case NullableShape(false, shape) => property.withRange(shape).withMinCount(1)
    }
  }
}
