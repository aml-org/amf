package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, KeyParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GraphQLNestedTypeParser(objTypeNode: Node, isInterface: Boolean = false)(implicit
    val ctx: GraphQLBaseWebApiContext
) extends GraphQLCommonTypeParser {
  val obj: NodeShape = NodeShape(toAnnotations(objTypeNode))

  def parse(): NodeShape = {
    val (name, annotations) = findName(objTypeNode, "AnonymousNestedType", "Missing name for root nested type")
    obj.withName(name, annotations)
    collectInheritance()
    collectFields()
    parseDescription(objTypeNode, obj, obj.meta)
    if (isInterface) {
      obj.set(NodeShapeModel.IsAbstract, AmfScalar(true), synthesized())
      inFederation { implicit fCtx =>
        FederationMetadataParser(
          objTypeNode,
          obj,
          Seq(INTERFACE_DIRECTIVE, INTERFACE_FEDERATION_DIRECTIVE),
          ShapeFederationMetadataFactory
        )
          .parse()
        GraphQLDirectiveApplicationsParser(objTypeNode, obj, Seq(INTERFACE_DIRECTIVE, DIRECTIVE)).parse()
        KeyParser(objTypeNode, obj, Seq(INTERFACE_DIRECTIVE, INTERFACE_FEDERATION_DIRECTIVE)).parse()
      }
    }
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        objTypeNode,
        obj,
        Seq(OBJECT_DIRECTIVE, OBJECT_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(objTypeNode, obj, Seq(OBJECT_DIRECTIVE, DIRECTIVE)).parse()
      KeyParser(objTypeNode, obj, Seq(OBJECT_DIRECTIVE, OBJECT_FEDERATION_DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(objTypeNode, obj).parse()
    obj
  }

  def collectFields(): Unit = collectFieldsFromPath(objTypeNode, Seq(FIELDS_DEFINITION, FIELD_DEFINITION))

  def collectInheritance(): Unit = {
    val ifaces = collect(objTypeNode, Seq(IMPLEMENTS_INTERFACES, NAMED_TYPE, NAME)).map { case ifaceName: Node =>
      parseInheritance(ifaceName.children.head.asInstanceOf[Terminal])
    }
    if (ifaces.nonEmpty) {
      obj.set(ShapeModel.Inherits, AmfArray(ifaces, inferred()), inferred())
    }
  }

  def parseInheritance(t: Terminal): AnyShape = {
    val typeName = t.value
    ctx.declarations.findType(typeName, SearchScope.All) match {
      case Some(n: NodeShape) =>
        val ann = toAnnotations(t)
        n.link(AmfScalar(t.value, ann), ann, ann).asInstanceOf[NodeShape].withName(typeName, ann)
      case _ =>
        unresolvedShape(typeName, t)
    }
  }
}
