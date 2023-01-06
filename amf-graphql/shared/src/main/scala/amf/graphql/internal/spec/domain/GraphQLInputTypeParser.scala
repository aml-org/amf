package amf.graphql.internal.spec.domain

import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.internal.annotations.InputTypeField
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.antlrast.ast.Node
import amf.graphql.internal.spec.document._

case class GraphQLInputTypeParser(objTypeNode: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLCommonTypeParser {
  val obj: NodeShape = NodeShape(toAnnotations(objTypeNode))

  def parse(): NodeShape = {
    val (name, annotations) = findName(objTypeNode, "AnonymousInputType", "Missing name for input type")
    obj.withName(name, annotations)
    obj set true as NodeShapeModel.InputOnly
    collectFields()
    addInputTypeFieldAnnotation()
    parseDescription(objTypeNode, obj, obj.meta)
    parseDirectives()
    parseFederationMetadata()
    obj
  }

  private def addInputTypeFieldAnnotation(): Unit = obj.properties.foreach(_.add(InputTypeField()))
  private def collectFields(): Unit =
    collectFieldsFromPath(objTypeNode, Seq(INPUT_FIELDS_DEFINITION, INPUT_VALUE_DEFINITION))

  private def parseDirectives(): Unit = {
    inFederation { implicit fCtx =>
      GraphQLDirectiveApplicationsParser(objTypeNode, obj, Seq(INPUT_OBJECT_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(objTypeNode, obj).parse()
  }

  private def parseFederationMetadata(): Unit = {
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        objTypeNode,
        obj,
        Seq(INPUT_OBJECT_DIRECTIVE, INPUT_OBJECT_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      )
        .parse()
    }
  }
}
