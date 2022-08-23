package amf.graphql.internal.spec.domain

import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{KeyParser, ShapeFederationMetadataParser}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
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
      obj.withIsAbstract(true)
      inFederation { implicit fCtx =>
        ShapeFederationMetadataParser(objTypeNode, obj, Seq(INTERFACE_DIRECTIVE, INTERFACE_FEDERATION_DIRECTIVE))
          .parse()
        GraphQLDirectiveApplicationParser(objTypeNode, obj, Seq(INTERFACE_DIRECTIVE, DIRECTIVE)).parse()
        KeyParser(objTypeNode, obj, Seq(INTERFACE_DIRECTIVE, INTERFACE_FEDERATION_DIRECTIVE)).parse()
      }
    }
    inFederation { implicit fCtx =>
      ShapeFederationMetadataParser(objTypeNode, obj, Seq(OBJECT_DIRECTIVE, OBJECT_FEDERATION_DIRECTIVE)).parse()
      GraphQLDirectiveApplicationParser(objTypeNode, obj, Seq(OBJECT_DIRECTIVE, DIRECTIVE)).parse()
      KeyParser(objTypeNode, obj, Seq(OBJECT_DIRECTIVE, OBJECT_FEDERATION_DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationParser(objTypeNode, obj).parse()
    obj
  }

  def collectFields(): Unit = collectFieldsFromPath(objTypeNode, Seq(FIELDS_DEFINITION, FIELD_DEFINITION))

  def collectInheritance(): Unit = {
    val ifaces = collect(objTypeNode, Seq(IMPLEMENTS_INTERFACES, NAMED_TYPE, NAME)).map { case ifaceName: Node =>
      parseInheritance(ifaceName.children.head.asInstanceOf[Terminal])
    }
    if (ifaces.nonEmpty) {
      obj.withInherits(ifaces)
    }
  }

  def parseInheritance(t: Terminal): AnyShape = {
    val typeName = t.value
    ctx.declarations.findType(typeName, SearchScope.All) match {
      case Some(i: NodeShape) if i.isAbstract.value() =>
        i.link(t.value, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case Some(n: NodeShape) =>
        astError("Error extending non interface type", toAnnotations(t))
        n.link(t.value, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case _ =>
        unresolvedShape(typeName, t)
    }
  }
}
