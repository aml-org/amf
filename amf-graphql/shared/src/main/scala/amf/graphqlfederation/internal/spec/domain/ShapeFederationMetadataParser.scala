package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.federation.{HasShapeFederationMetadata, ShapeFederationMetadata}
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import org.mulesoft.antlrast.ast.Node

case class ShapeFederationMetadataParser(ast: Node, target: HasShapeFederationMetadata, basePath: Seq[String])(implicit
    val ctx: GraphQLFederationWebApiContext
) extends GraphQLASTParserHelper {
  def parse(): Unit = {
    parseOverride()
    parseInaccessible()
    parseShareable()
  }

  protected def parseOverride(): Unit = {
    pathToNonTerminal(ast, basePath :+ OVERRIDE_DIRECTIVE)
      .map(findName(_, "default-from", "ERR"))
      .foreach { overrideTarget =>
        in { metadata =>
          metadata.withOverrideFrom(overrideTarget)
        }
      }
  }

  protected def parseShareable(): Unit = {
    pathToNonTerminal(ast, basePath :+ SHAREABLE_DIRECTIVE)
      .foreach { _ =>
        in { metadata =>
          metadata.withShareable(true)
        }
      }
  }

  protected def parseInaccessible(): Unit = {
    pathToNonTerminal(ast, basePath :+ INACCESSIBLE_DIRECTIVE)
      .foreach { _ =>
        in { metadata =>
          metadata.withInaccessible(true)
        }
      }
  }

  protected def in(fn: (ShapeFederationMetadata) => Unit): Unit = {
    Option(target.federationMetadata) match {
      case Some(metadata) => fn(metadata)
      case None =>
        val metadata = ShapeFederationMetadata()
        target.withFederationMetadata(metadata)
        fn(metadata)
    }
  }
}
