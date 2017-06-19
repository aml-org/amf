package amf.json

import amf.common.AMFToken.{Link, Root, WhiteSpace}
import amf.common.{AMFAST, AMFASTLink, AMFASTNode, AMFToken}
import amf.lexer.CharStream
import amf.parser.{BaseASTBuilder, Range}
import amf.remote.{Context, Platform}
import amf.common.Strings.strings

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/23/17.
  */
class JsonCompiler private (val url: String, val remote: Platform, val base: Option[Context]) {

  private lazy val context: Context                = base.map(_.update(url)).getOrElse(Context(url))
  private var root: AMFAST                         = _
  private val includes: ListBuffer[Future[AMFAST]] = ListBuffer()

  def build(): Future[AMFAST] = {
    remote.resolve(url, base).flatMap(parse)
  }

  private def parse(stream: CharStream): Future[AMFAST] = {
    val builder = new JsonASTBuilder(JsonLexer(stream))
    val parser  = new JsonParser(builder)

    if (root == null) {
      root = builder.root() {
        parser.parse
      }
    }

    Future.sequence(includes).map(_ => root)
  }

  class JsonASTBuilder(lexer: JsonLexer) extends BaseASTBuilder[AMFToken, AMFAST](lexer) {
    override protected def createNode(token: AMFToken, content: String, range: Range): AMFAST =
      new AMFASTNode(token, content, range)

    override protected def createNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = token match {
      case Link => createLinkNode(token, range, children)
      case _    => createJsonNode(token, range, children)
    }

    /** Build and return root node. */
    override def root()(parse: () => Unit): AMFAST = {
      beginNode()
      parse()
      buildAST(Root)
    }

    override protected def accepts(token: AMFToken): Boolean = token != WhiteSpace
  }

  private def createLinkNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val include = children.head
    val url     = include.content.unquote
    new AMFASTLink(url, include.range, remote, context).resolve(future => includes += future)
  }

  private def createJsonNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val start = children.head.range
    val end   = children.last.range
    new AMFASTNode(token, null, start.extent(end), children)
  }
}

object JsonCompiler {
  def apply(url: String, remote: Platform, context: Option[Context] = None) = new JsonCompiler(url, remote, context)
}
