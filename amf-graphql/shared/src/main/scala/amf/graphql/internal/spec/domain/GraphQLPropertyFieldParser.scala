package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, NullableShape}
import amf.graphqlfederation.internal.spec.domain.{
  ExternalDirectiveParser,
  ProvidesParser,
  RequiresParser,
  ShapeFederationMetadataParser
}
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

case class GraphQLPropertyFieldParser(ast: Node, parent: NodeShape)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val property: PropertyShape = PropertyShape(toAnnotations(ast))

  def parse(setterFn: PropertyShape => Unit): Unit = {
    parseName()
    setterFn(property)
    parseDescription(ast, property, property.meta)
    parseRange()
    inFederation { implicit fCtx =>
      ShapeFederationMetadataParser(ast, property, Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE)).parse()
      ShapeFederationMetadataParser(ast, property, Seq(INPUT_VALUE_DIRECTIVE, INPUT_FIELD_FEDERATION_DIRECTIVE)).parse()
      GraphQLDirectiveApplicationsParser(ast, property, Seq(FIELD_DIRECTIVE, DIRECTIVE)).parse()
      GraphQLDirectiveApplicationsParser(ast, property, Seq(INPUT_VALUE_DIRECTIVE, DIRECTIVE)).parse()
      ExternalDirectiveParser(ast, property).parse()
      ProvidesParser(ast, property).parse()
      RequiresParser(ast, property, parent).parse()
    }
    GraphQLDirectiveApplicationsParser(ast, property).parse()
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(ast, "AnonymousField", "Missing name for field")
    property.withName(name, annotations)
  }

  private def parseRange(): Unit = {
    val range = parseType(ast)
    unpackNilUnion(range) match {
      case NullableShape(true, shape)  => property.withRange(shape).withMinCount(0)
      case NullableShape(false, shape) => property.withRange(shape).withMinCount(1)
    }
  }
}
