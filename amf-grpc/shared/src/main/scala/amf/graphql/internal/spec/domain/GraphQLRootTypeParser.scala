package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{FIELDS_DEFINITION, FIELD_DEFINITION}
import org.mulesoft.antlrast.ast.Node

case class GraphQLRootTypeParser(ast: Node, queryType: GraphQLWebApiContext.RootTypes.Value)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {

  val rootTypeName = findName(ast, "AnonymousType", "", "Missing name for root type")

  def parse(adopt: EndPoint => Unit): Seq[EndPoint] = {
    parseFields(ast, adopt)
  }

   private def parseFields(n: Node, adopt: EndPoint => Unit): Seq[EndPoint] = {
    collect(n, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).map { case f:Node =>
      parseField(f, adopt)
    }
  }

  private def parseField(f: Node, adopt: EndPoint => Unit) = {
    val endPoint: EndPoint = EndPoint(toAnnotations(ast))
    val fieldName = findName(f, "AnonymousField", "", "Missing name for root type field")
    val endpointPath = s"${rootTypeName}/${fieldName}"
    endPoint.withPath(endpointPath).withName(s"${rootTypeName}.${fieldName}")
    adopt(endPoint)
    findDescription(f).foreach { description =>
      endPoint.withDescription(description.value)
    }
    endPoint
  }
}
