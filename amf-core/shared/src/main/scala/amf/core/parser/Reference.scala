package amf.core.parser

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context}
import amf.core.services.RuntimeCompiler
import amf.internal.environment.Environment
import org.yaml.model.YNode

import scala.collection.mutable
import scala.concurrent.Future

case class Reference(url: String, refs: Seq[RefContainer]) {

  def isRemote: Boolean = !url.startsWith("#")

  def +(kind: ReferenceKind, ast: YNode): Reference = {
    copy(refs = refs :+ RefContainer(kind, ast))
  }
  def resolve(base: Context,
              mediaType: Option[String],
              vendor: String,
              cache: Cache,
              ctx: ParserContext,
              env: Environment): Future[BaseUnit] = {
    val kinds = refs.map(_.linkType)
    val kind  = if (kinds.distinct.size > 1) UnspecifiedReference else kinds.distinct.head
    RuntimeCompiler(url, mediaType, vendor, base, kind, cache, Some(ctx), env)
  }
}

object Reference {
  def apply(url: String, kind: ReferenceKind, node: YNode): Reference =
    new Reference(url, Seq(RefContainer(kind, node)))
}

case class RefContainer(linkType: ReferenceKind, node: YNode)

case class ReferenceCollector() {
  private val refs = mutable.Map[String, Reference]()

  def +=(key: String, kind: ReferenceKind, node: YNode): Unit = {
    refs.get(key) match {
      case Some(reference: Reference) => refs.update(key, reference + (kind, node))
      case None                       => refs += key -> Reference(key, kind, node)
    }
  }

  def toReferences: Seq[Reference] = refs.values.toSeq
}

object EmptyReferenceCollector extends ReferenceCollector {}
