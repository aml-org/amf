package amf.graphql.internal.spec.domain

import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{NAME, NAMED_TYPE, UNION_MEMBER_TYPES}
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GraphQLNestedUnionParser(unionTypeDef: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val union = UnionShape(toAnnotations(unionTypeDef))

  def parse(): UnionShape = {
    parseName()
    parseMembers()
    union
  }

  private def parseMembers(): Unit = {
    val members = collect(unionTypeDef, Seq(UNION_MEMBER_TYPES, NAMED_TYPE, NAME)).flatMap { case n: Node =>
      n.children.collectFirst {
        case t: Terminal =>
          val memberName = t.value
          Some(findOrLinkType(memberName, t))
        case _ =>
          astError("Missing union member", toAnnotations(n))
          None
      }
    }

    val finalMembers: Seq[AnyShape] = members.collect { case Some(t) => t }
    union.withAnyOf(finalMembers)
  }

  private def parseName(): Unit = {
    val name = findName(unionTypeDef, "AnonymousUnion", "Missing union type name")
    union.withName(name)
  }
}
