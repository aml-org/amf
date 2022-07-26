package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.DataType
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.ShapeFederationMetadataParser
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.Node

class GraphQLCustomScalarParser(customScalarTypeDef: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val scalar: ScalarShape = ScalarShape(toAnnotations(customScalarTypeDef))

  def parse(): ScalarShape = {
    scalar.withDataType(DataType.String)
    parseNameAndFormat()
    parseDescription(customScalarTypeDef, scalar, scalar.meta)
    inFederation { implicit fCtx =>
      ShapeFederationMetadataParser(customScalarTypeDef, scalar, Seq(SCALAR_DIRECTIVE, SCALAR_FEDERATION_DIRECTIVE)).parse()
      GraphQLDirectiveApplicationParser(customScalarTypeDef, scalar, Seq(SCALAR_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationParser(customScalarTypeDef, scalar).parse()
    scalar
  }

  private def parseNameAndFormat(): Unit = {
    val (name, annotations) = findName(customScalarTypeDef, "AnonymousScalar", "Missing scalar type name")
    scalar.withName(name, annotations)
    scalar.withFormat(name)
  }

}
