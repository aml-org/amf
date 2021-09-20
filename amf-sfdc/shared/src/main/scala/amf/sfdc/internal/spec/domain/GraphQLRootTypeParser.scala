package amf.sfdc.internal.spec.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.internal.parser.domain.Value
import amf.sfdc.internal.spec.context.GraphQLWebApiContext
import amf.sfdc.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.sfdc.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.sfdc.internal.spec.parser.syntax.TokenTypes.{FIELDS_DEFINITION, FIELD_DEFINITION}
import org.mulesoft.antlrast.ast.Node

case class GraphQLRootTypeParser(ast: Node, queryType: RootTypes.Value)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {

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
    val endPoint: EndPoint = EndPoint(toAnnotations(f))
    val fieldName = findName(f, "AnonymousField", "", "Missing name for root type field")
    val endpointPath = s"${rootTypeName}/${fieldName}"
    endPoint.withPath(endpointPath).withName(s"${rootTypeName}.${fieldName}")
    adopt(endPoint)
    findDescription(f).foreach { description =>
      endPoint.withDescription(description.value)
    }
    parseOperation(f, endPoint, fieldName)
    endPoint
  }

  def parseOperation(f: Node, endPoint: EndPoint, fieldName: String) = {
    val operationId = s"${rootTypeName}.${fieldName}"

    val method = queryType match {
      case RootTypes.Query        => "query"
      case RootTypes.Mutation     => "post"
      case RootTypes.Subscription => "subscribe"
    }

    val op = endPoint.withOperation(method).withName(operationId).withOperationId(operationId)
  }
}
