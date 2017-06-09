package amf.yaml

import java.util.function.Consumer

import amf.parser._
import amf.remote.{Context, Platform}
import amf.visitor.ASTNodeVisitor
import amf.yaml.YamlAST.EMPTY_NODE
import amf.yaml.YamlToken.{Eof, Link}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pedro.colunga on 5/23/17.
  */
trait YamlAST extends ASTNode[YamlToken] {
    override type N = YamlAST
}

class YamlASTNode(token: YamlToken, content: String, range : Range, override val children: Seq[YamlAST] = Seq(), override val annotations: Seq[Annotation] = Seq()) extends BaseASTNode[YamlToken](token, content, range) with YamlAST {
    override val empty = EMPTY_NODE
}

class YamlASTLink(include: String, range : Range, remote: Platform, context: Context) extends BaseASTNode[YamlToken](Link, include, range) with ASTLinkNode[YamlToken] with YamlAST {
    override val annotations: Seq[Annotation] = Seq(IncludeAnnotation(include))
    override def children: Seq[YamlAST] = Seq()
    override def target: Document = document

    private var document: Document = _

    def resolve(futures: Consumer[Future[YamlAST]]): YamlASTLink = {
        futures.accept({
            YamlCompiler(include, remote, Some(context)).build().map(root => {
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


object YamlAST {
    val EMPTY_NODE = new YamlASTNode(Eof, "", Range.NONE)
}
