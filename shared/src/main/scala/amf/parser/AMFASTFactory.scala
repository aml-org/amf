package amf.parser

import amf.common.{AMFAST, AMFASTLink, AMFASTNode, AMFToken}
import amf.common.AMFToken.{Library, Link}
import amf.common.core.Strings
import amf.parser
import amf.remote.Kind

/**
  * AMF [[ASTFactory]]
  */
case class AMFASTFactory() extends ASTFactory[AMFToken, AMFAST] {

  /** Create [[amf.common.AMFAST]] node with given content. */
  override def createNode(token: AMFToken, content: String, range: parser.Range): AMFAST =
    new AMFASTNode(token, content, range)

  /** Create [[amf.common.AMFAST]] node with given children. */
  override def createNode(token: AMFToken, range: parser.Range, children: Seq[AMFAST]): AMFAST = token match {
    case Link | Library => createLinkNode(range, children, Kind(token))
    case _              => createYamlNode(token, range, children)
  }

  private def createYamlNode(token: AMFToken, range: parser.Range, children: Seq[AMFAST]): AMFAST = {
    val start = if (children.nonEmpty) children.head.range else range
    val end   = if (children.nonEmpty) children.last.range else range
    new AMFASTNode(token, null, start.extent(end), children)
  }

  private def createLinkNode(range: Range, children: Seq[AMFAST], source: Kind): AMFAST = {
    val url  = children.head.content.unquote
    val link = new AMFASTLink(url, range, source)
    //      references += link
    link
  }
}
