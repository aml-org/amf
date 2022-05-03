package amf.graphql.internal.spec.domain

import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, UnresolvedShape}
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GraphQLNestedTypeParser(objTypeNode: Node, isInterface: Boolean = false)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLCommonTypeParser {
  val obj: NodeShape = NodeShape(toAnnotations(objTypeNode))

  def parse(parentId: String): NodeShape = {
    obj.adopted(parentId)
    val name = findName(objTypeNode, "AnonymousNestedType", "", "Missing name for root nested type")
    obj.withName(name).adopted(parentId)
    collectInheritance()
    collectFields()
    if (isInterface) {
      obj.withIsAbstract(true)
    }
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
      case Some(i: NodeShape) if i.isAbstract.option().getOrElse(false) =>
        i.link(t.value, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case Some(n: NodeShape) =>
        astError(obj.id, "Error extending non interface type", toAnnotations(t))
        n.link(t.value, toAnnotations(t)).asInstanceOf[NodeShape].withName(typeName, toAnnotations(t))
      case _ =>
        val shape = UnresolvedShape(typeName, toAnnotations(t))
        shape.withContext(ctx)
        shape.unresolved(typeName, Nil, Some(elementSourceLocation(t)))
        shape
    }
  }
}
