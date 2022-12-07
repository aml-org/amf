package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import org.mulesoft.antlrast.ast.Node

class GraphQLCustomScalarParser(customScalarTypeDef: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val scalar: ScalarShape = ScalarShape(toAnnotations(customScalarTypeDef))

  def parse(): ScalarShape = {
    scalar.set(ScalarShapeModel.DataType, AmfScalar(DataType.Any), synthesized())
    parseName()
    parseDescription(customScalarTypeDef, scalar, scalar.meta)
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        customScalarTypeDef,
        scalar,
        Seq(SCALAR_DIRECTIVE, SCALAR_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(customScalarTypeDef, scalar, Seq(SCALAR_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(customScalarTypeDef, scalar).parse()
    scalar
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(customScalarTypeDef, "AnonymousScalar", "Missing scalar type name")
    scalar.withName(name, annotations)
  }
}
