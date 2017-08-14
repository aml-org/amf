package amf.parser

import amf.common.AMFToken.{Library, Link, Root}
import amf.common.Strings.strings
import amf.common._
import amf.lexer.Lexer
import amf.remote.Kind

import scala.collection.mutable.ListBuffer

class YeastASTBuilder private (lexer: Lexer[AMFToken], url: String) extends BaseASTBuilder[AMFToken, AMFAST](lexer) {

  val references: ListBuffer[AMFASTLink] = ListBuffer()

  override protected def createNode(token: AMFToken, content: String, range: Range): AMFAST =
    new AMFASTNode(token, content, range)

  override protected def createNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST =
    token match {
      case Link | Library => createLinkNode(range, children, Kind(token))
      case _              => createYeastNode(token, range, children)
    }

  /** Build and return root node. */
  override def root()(parse: () => Unit): AMFAST = {
    beginNode()
    parse()
    buildAST(Root)
  }

  private def createLinkNode(range: Range, children: Seq[AMFAST], source: Kind): AMFAST = {
    val url  = children.head.content.unquote
    val link = new AMFASTLink(url, range, source)
    references += link
    link
  }

  private def createYeastNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val start = if (children.nonEmpty) children.head.range else range
    val end   = if (children.nonEmpty) children.last.range else range
    new AMFASTNode(token, null, start.extent(end), children)
  }

  /** Collect specified error. */
  override def error(message: String): Unit = println(s"Error at $url:${lexer.currentRange} => $message")
}

object YeastASTBuilder {
  def apply(lexer: Lexer[AMFToken], url: String = ""): YeastASTBuilder =
    new YeastASTBuilder(lexer, url)
}
