package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.AmfArray
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.document._
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import amf.shapes.internal.domain.metamodel.UnionShapeModel
import org.mulesoft.antlrast.ast.{Node, Terminal}
class GraphQLNestedUnionParser(unionTypeDef: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val union: UnionShape = UnionShape(toAnnotations(unionTypeDef))

  def parse(): UnionShape = {
    parseName()
    parseMembers()
    parseDescription(unionTypeDef, union, union.meta)
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        unionTypeDef,
        union,
        Seq(UNION_DIRECTIVE, UNION_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(unionTypeDef, union, Seq(UNION_DIRECTIVE, DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationsParser(unionTypeDef, union).parse()
    union
  }

  private def parseMembers(): Unit = {
    collect(unionTypeDef, Seq(UNION_MEMBER_TYPES)).map { case membersNode: Node =>
      val members: Seq[Option[AnyShape]] = collect(membersNode, Seq(NAMED_TYPE, NAME)).flatMap { case n: Node =>
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
      union synthetically () set AmfArray(finalMembers, toAnnotations(membersNode)) as UnionShapeModel.AnyOf
    }
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(unionTypeDef, "AnonymousUnion", "Missing union type name")
    union.withName(name, annotations)
  }
}
