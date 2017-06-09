package amf.json

import java.util.function.Consumer

import amf.json.JsonAST.EMPTY_NODE
import amf.json.JsonToken.{Eof, Link}
import amf.parser._
import amf.remote.{Context, Platform}
import amf.visitor.ASTNodeVisitor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait JsonAST extends ASTNode[JsonToken] {
    override type N = JsonAST
}

class JsonASTNode(token: JsonToken, content: String, range : Range, override val children: Seq[JsonAST] = Seq(), override val annotations: Seq[Annotation] = Seq()) extends BaseASTNode[JsonToken](token, content, range) with JsonAST {
    override val empty = EMPTY_NODE
}

class JsonASTLink(include: String, range : Range, remote: Platform, context: Context) extends BaseASTNode[JsonToken](Link, include, range) with ASTLinkNode[JsonToken] with JsonAST {
    override val annotations: Seq[Annotation] = Seq(IncludeAnnotation(include))
    override def children: Seq[JsonAST] = Seq()
    override def target: Document = document

    private var document: Document = _

    def resolve(futures: Consumer[Future[JsonAST]]): JsonASTLink = {
        futures.accept({
            JsonCompiler(include, remote, Some(context)).build().map(root => {
                document = Document(root, include)
                root
            })
        })
        this
    }

    /** Accept given node visitor. */
    override def accept(visitor: ASTNodeVisitor): Unit = {
        visitor.before(this)
        visitor.visit(this)
        visitor.after(this)
    }

    override val empty = EMPTY_NODE
}


object JsonAST {
    val EMPTY_NODE = new JsonASTNode(Eof, "", Range.NONE)
}
