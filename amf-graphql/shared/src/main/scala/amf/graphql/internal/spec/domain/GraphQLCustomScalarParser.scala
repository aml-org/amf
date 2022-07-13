package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.DataType
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.Node

class GraphQLCustomScalarParser(customScalarTypeDef: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val scalar: ScalarShape = ScalarShape(toAnnotations(customScalarTypeDef))

  def parse(): ScalarShape = {
    scalar.withDataType(DataType.String)
    parseNameAndFormat()
    GraphQLDirectiveApplicationParser(customScalarTypeDef, scalar).parse()
    scalar
  }

  private def parseNameAndFormat(): Unit = {
    val (name, annotations) = findName(customScalarTypeDef, "AnonymousScalar", "Missing scalar type name")
    scalar.withName(name, annotations)
    scalar.withFormat(name)
  }

}
