package amf.yaml

import amf.common.AMFToken.{Link, Root}
import amf.common.{AMFAST, AMFASTLink, AMFASTNode, AMFToken}
import amf.common.Strings.strings
import amf.lexer.CharStream
import amf.parser.{BaseASTBuilder, Range}
import amf.remote.{Context, Platform}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/23/17.
  */
class YamlCompiler private (val url: String, val remote: Platform, val base: Option[Context]) {

  private lazy val context: Context                = base.map(_.update(url)).getOrElse(Context(url))
  private var root: AMFAST                         = _
  private val includes: ListBuffer[Future[AMFAST]] = ListBuffer()

  def build(): Future[AMFAST] = {
    remote.resolve(url, base).flatMap(parse)
  }

  private def parse(stream: CharStream): Future[AMFAST] = {
    val builder = new YamlASTBuilder(YamlLexer(stream))
//    val parser  = new YamlParser(builder)

    if (root == null) {
//      root = builder.root() {
//        parser.parse
//      }
    }

    Future.sequence(includes).map(_ => root)
  }

  class YamlASTBuilder(lexer: YamlLexer) extends BaseASTBuilder[AMFToken, AMFAST](lexer) {
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
  }

  private def createLinkNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val url = children.head.content.unquote
    new AMFASTLink(url, range) /*.resolve(future => includes += future)*/
  }

  private def createYamlNode(token: AMFToken, range: Range, children: Seq[AMFAST]): AMFAST = {
    val start = children.head.range
    val end   = children.last.range
    new AMFASTNode(token, null, start.extent(end), children)
  }
}

object YamlCompiler {
  def apply(url: String, remote: Platform, context: Option[Context] = None) = new YamlCompiler(url, remote, context)
}
