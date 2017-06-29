package amf.common

import amf.common.AMFToken.{Eof, Link}
import amf.compiler.AMFCompiler
import amf.parser.{AMFUnit, AMFUnitType, ASTLinkNode, ASTNode, Annotation, BaseASTNode, IncludeAnnotation, Range}
import amf.remote._
import amf.visitor.ASTNodeVisitor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by hernan.najles on 6/16/17.
  */
trait AMFAST extends ASTNode[AMFToken] {
  override type N = AMFAST
}

class AMFASTNode(token: AMFToken,
                 content: String,
                 range: Range,
                 override val children: Seq[AMFAST] = Seq(),
                 override val annotations: Seq[Annotation] = Seq())
    extends BaseASTNode[AMFToken](token, content, range)
    with AMFAST {
  override val empty = AMFAST.EMPTY_NODE
}

class AMFASTLink(include: String, range: Range, containerType: AMFUnitType)
    extends BaseASTNode[AMFToken](Link, include, range)
    with ASTLinkNode[AMFToken]
    with AMFAST {

  override val annotations: Seq[Annotation] = Seq(IncludeAnnotation(include))

  override def children: Seq[AMFAST] = Seq()

  override def target: AMFUnit = container

  private var container: AMFUnit = _

  def resolve(remote: Platform, context: Context, cache: Cache, hint: Option[Hint]): Future[AMFAST] = {
    AMFCompiler(include, remote, hint, Some(context), Some(cache))
      .build()
      .map(root => {
        container = AMFUnit(root, include, containerType, hint.map(_.vendor).getOrElse(Raml))
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
