package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION}
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.core.{ShapeOperation, ShapeRequest}
import org.mulesoft.antlrast.ast.Node

case class GraphQLFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {
  val property = PropertyShape(toAnnotations(ast))

  def parse(parentId: String): PropertyShape = {
    parseName()
    property.adopted(parentId)
    parseDescription()
    parseArguments() match {
      case Some(n) =>
        property.withRange(n)
      case _ =>
        property.withRange(parseType(ast, property.id))
    }
    // pareOutputType @TODO
    property
  }

  private def parseName(): Unit = {
    property.withName(findName(ast, "AnonymousField", "", "Missing name for field"))
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(_.value).foreach(property.withDescription)
  }

  private def parseArguments() = {
    val shape1  = NodeShape().adopted(property.id)
    val op      = shape1.withOperation("default")
    val request = op.withRequest()
    val params = collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map {
      case argument: Node =>
        parseArgument(argument, request)
    }
    if (params.nonEmpty) {
      val payload = op.withResponse("default").withPayload(Some("application/graphql"))
      payload.withSchema(parseType(ast, payload.id))
      Some(shape1)
    } else None
  }

  private def parseArgument(n: Node, request: ShapeRequest) = {
    val name = findName(n, "AnonymousInputType", property.id, "Missing input type name")

    val description = findDescription(n) // where?
    val param       = request.withQueryParameter(name)
    param.withSchema(parseType(n, param.id))

    // add parameters to propertyShape model @TODO
  }
}
