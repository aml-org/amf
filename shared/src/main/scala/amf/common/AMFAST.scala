package amf.common

import amf.common.AMFToken.{Eof, Link}
import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.parser.{ASTLinkNode, ASTNode, BaseASTNode, Range}
import amf.remote._
import amf.visitor.ASTNodeVisitor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  */
trait AMFAST extends ASTNode[AMFToken] {
  override type N = AMFAST
}

class AMFASTNode(token: AMFToken, content: String, range: Range, override val children: Seq[AMFAST] = Seq())
    extends BaseASTNode[AMFToken](token, content, range)
    with AMFAST {
  override val empty = AMFAST.EMPTY_NODE
}

class AMFASTLink(include: String, range: Range, source: Kind)
    extends BaseASTNode[AMFToken](Link, include, range)
    with ASTLinkNode[AMFToken]
    with AMFAST {

  override def children: Seq[AMFAST] = Seq()

  var target: BaseUnit = _

  def resolve(remote: Platform, context: Context, cache: Cache, hint: Hint): Future[BaseUnit] = {
    AMFCompiler(include, remote, hint + source, Some(context), Some(cache))
      .build()
      .map(root => {
        target = root
        root
      })
  }

  /** Accept given node visitor. */
  override def accept(visitor: ASTNodeVisitor): Unit = {
    visitor.before(this)
    visitor.visit(this)
    visitor.after(this)
  }

  override val empty = AMFAST.EMPTY_NODE
}

object AMFAST {
  val EMPTY_NODE = new AMFASTNode(Eof, "", Range.NONE)
}
