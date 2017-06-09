package amf.json

import amf.common.Strings.strings
import amf.json.JsonToken.{Link, Root, WhiteSpace}
import amf.lexer.CharStream
import amf.parser.{BaseASTBuilder, Range}
import amf.remote.{Context, Platform}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/23/17.
  */
class JsonCompiler private(val url: String, val remote: Platform, val base: Option[Context]) {

    private lazy val context: Context = base.map(_.update(url)).getOrElse(Context(url))
    private var root: JsonAST = _
    private val includes: ListBuffer[Future[JsonAST]] = ListBuffer()

    def build(): Future[JsonAST] = {
        remote.resolve(url, base).flatMap(parse)
    }

    private def parse(stream: CharStream): Future[JsonAST] = {
        val builder = new JsonASTBuilder(JsonLexer(stream))
        val parser = new JsonParser(builder)

        if (root == null) {
            root = builder.root() {
                parser.parse
            }
        }

        Future.sequence(includes).map(_ => root)
    }

    class JsonASTBuilder(lexer: JsonLexer) extends BaseASTBuilder[JsonToken, JsonAST](lexer) {
        override protected def createNode(token: JsonToken, content: String, range : Range): JsonAST = new JsonASTNode(token, content, range)

        override protected def createNode(token: JsonToken, range : Range, children: Seq[JsonAST]): JsonAST = token match {
            case Link => createLinkNode(token, range, children)
            case _ => createJsonNode(token, range, children)
        }

        /** Build and return root node. */
        override def root()(parse: () => Unit): JsonAST = {
            beginNode()
            parse()
            buildAST(Root)
        }

        override protected def accepts(token: JsonToken): Boolean = token != WhiteSpace
    }

    private def createLinkNode(token: JsonToken, range : Range, children: Seq[JsonAST]): JsonAST = {
        val include = children.head
        val url = include.content.unquote
        new JsonASTLink(url, include.range, remote, context).resolve(future => includes += future)
    }

    private def createJsonNode(token: JsonToken, range : Range, children: Seq[JsonAST]): JsonAST = {
        val start = children.head.range
        val end = children.last.range
        new JsonASTNode(token, null, start.extent(end), children)
    }
}

object JsonCompiler {
    def apply(url: String, remote: Platform, context: Option[Context] = None) = new JsonCompiler(url, remote, context)
}
