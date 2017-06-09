package amf.yaml

import amf.common.Strings.strings
import amf.lexer.CharStream
import amf.parser.{BaseASTBuilder, Range}
import amf.remote.{Context, Platform}
import amf.yaml.YamlToken._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/23/17.
  */
class YamlCompiler private(val url: String, val remote: Platform, val base: Option[Context]) {

    private lazy val context: Context = base.map(_.update(url)).getOrElse(Context(url))
    private var root: YamlAST = _
    private val includes: ListBuffer[Future[YamlAST]] = ListBuffer()

    def build(): Future[YamlAST] = {
        remote.resolve(url, base).flatMap(parse)
    }

    private def parse(stream: CharStream): Future[YamlAST] = {
        val builder = new YamlASTBuilder(YamlLexer(stream))
        val parser = new YamlParser(builder)

        if (root == null) {
            root = builder.root() {
                parser.parse
            }
        }

        Future.sequence(includes).map(_ => root)
    }

    class YamlASTBuilder(lexer: YamlLexer) extends BaseASTBuilder[YamlToken, YamlAST](lexer) {
        override protected def createNode(token: YamlToken, content: String, range : Range): YamlAST = new YamlASTNode(token, content, range)

        override protected def createNode(token: YamlToken, range : Range, children: Seq[YamlAST]): YamlAST = token match {
            case Link => createLinkNode(token, range, children)
            case _ => createYamlNode(token, range, children)
        }

        /** Build and return root node. */
        override def root()(parse: () => Unit): YamlAST = {
            beginNode()
            parse()
            buildAST(Root)
        }
    }

    private def createLinkNode(token: YamlToken, range : Range, children: Seq[YamlAST]): YamlAST = {
        val url = children.head.content.unquote
        new YamlASTLink(url, range, remote, context).resolve(future => includes += future)
    }

    private def createYamlNode(token: YamlToken, range : Range, children: Seq[YamlAST]): YamlAST = {
        val start = children.head.range
        val end = children.last.range
        new YamlASTNode(token, null, start.extent(end), children)
    }
}

object YamlCompiler {
    def apply(url: String, remote: Platform, context: Option[Context] = None) = new YamlCompiler(url, remote, context)
}
