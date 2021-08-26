package amf.graphql.internal.spec.domain

import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{FIELDS_DEFINITION, FIELD_DEFINITION}
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

class GraphQLNestedTypeParser(objTypeNode: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  def parse(parentId: String): NodeShape = {
    val obj  = NodeShape()
    val name = findName(objTypeNode, "AnonymousNestedType", "", "Missing name for root nested type")
    obj.withName(name).adopted(parentId)
    collect(objTypeNode, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).foreach {
      case fieldNode: Node =>
        GraphQLFieldParser(fieldNode).parse(obj.id)
      case _ => // ignore
    }
    obj
  }

}
