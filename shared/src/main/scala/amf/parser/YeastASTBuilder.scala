package amf.parser

import amf.common.AMFToken.{Link, Root}
import amf.common.Strings.strings
import amf.common.{AMFAST, AMFASTLink, AMFASTNode, AMFToken}
import amf.lexer.Lexer

import scala.collection.mutable.ListBuffer

class YeastASTBuilder private (lexer: Lexer[AMFToken]) extends BaseASTBuilder[AMFToken, AMFAST](lexer) {

  val references: ListBuffer[AMFASTLink] = ListBuffer()

  override protected def createNode(token: AMFToken, content: String, range: Range): AMFAST =
    new AMFASTNode(token, content, range)

  override protected def createNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = token match {
    case Link => createLinkNode(token, range, children)
    case _    => createYamlNode(token, range, children)
  }

  /** Build and return root node. */
  override def root()(parse: () => Unit): AMFAST = {
    beginNode()
    parse()
    buildAST(Root)
  }

  private def createLinkNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val url  = children.head.content.unquote
    val link = new AMFASTLink(url, range)
    references += link
    link
  }

  private def createYamlNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val start = children.head.range
    val end   = children.last.range
    new AMFASTNode(token, null, start.extent(end), children)
  }
}

object YeastASTBuilder {
  def apply(lexer: Lexer[AMFToken]): YeastASTBuilder = new YeastASTBuilder(lexer)
}
