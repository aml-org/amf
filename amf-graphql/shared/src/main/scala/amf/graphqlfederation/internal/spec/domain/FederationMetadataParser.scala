package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.federation.{FederationMetadata, HasFederationMetadata}
import amf.core.internal.metamodel.domain.federation.FederationMetadataModel.OverrideFrom
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import org.mulesoft.antlrast.ast.Node

case class FederationMetadataParser[T <: FederationMetadata](
    ast: Node,
    target: HasFederationMetadata[T],
    basePath: Seq[String],
    factory: FederationMetadataFactory[T]
)(implicit val ctx: GraphQLFederationWebApiContext)
    extends GraphQLASTParserHelper {
  def parse(): Unit = {
    parseOverride()
    parseInaccessible()
    parseShareable()
  }

  protected def parseOverride(): Unit = {
    collectNodes(ast, basePath :+ OVERRIDE_DIRECTIVE).headOption
      .map(findName(_, "default-from", "ERR"))
      .foreach { case _ @(overrideTarget, annotations) =>
        in { metadata =>
          metadata.set(OverrideFrom, overrideTarget, annotations)
        }
      }
  }

  protected def parseShareable(): Unit = {
    collectNodes(ast, basePath :+ SHAREABLE_DIRECTIVE).headOption
      .foreach { _ =>
        in { metadata =>
          metadata.withShareable(true)
        }
      }
  }

  protected def parseInaccessible(): Unit = {
    collectNodes(ast, basePath :+ INACCESSIBLE_DIRECTIVE).headOption
      .foreach { _ =>
        in { metadata =>
          metadata.withInaccessible(true)
        }
      }
  }

  protected def in(fn: T => Unit): Unit = {
    Option(target.federationMetadata) match {
      case Some(metadata) => fn(metadata)
      case None =>
        val metadata = factory.create()
        target.withFederationMetadata(metadata)
        fn(metadata)
    }
  }
}
