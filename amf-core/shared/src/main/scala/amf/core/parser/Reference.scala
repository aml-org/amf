package amf.core.parser

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context}
import amf.core.services.RuntimeCompiler
import amf.internal.environment.Environment
import org.yaml.model.YNode
import amf.core.utils.Strings
import scala.collection.mutable
import scala.concurrent.Future

case class Reference(url: String, refs: Seq[RefContainer]) {

  def isRemote: Boolean = !url.startsWith("#")

  def +(kind: ReferenceKind, ast: YNode, fragment: Option[String]): Reference = {
    copy(refs = refs :+ RefContainer(kind, ast, fragment))
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

  def isInferred(): Boolean = refs.exists(_.linkType == InferredLinkReference)
}

object Reference {
  def apply(url: String, kind: ReferenceKind, node: YNode, fragment: Option[String]): Reference =
    new Reference(url, Seq(RefContainer(kind, node, fragment)))
}

case class RefContainer(linkType: ReferenceKind, node: YNode, fragment: Option[String])

case class ReferenceCollector() {
  private val refs = mutable.Map[String, Reference]()

  def +=(key: String, kind: ReferenceKind, node: YNode): Unit = {
    val (url, fragment) = checkFragment(key)
    refs.get(url) match {
      case Some(reference: Reference) => refs.update(key, reference + (kind, node, fragment))
      case None                       => refs += key -> Reference(key, kind, node, fragment)
    }
  }

  private def checkFragment(url: String): (String, Option[String]) = {
    if (url.normalizeUrl.startsWith("file://")) { // http urls supports # refs
      url.split("#") match { // how can i know if the # its part of the uri or not? uri not valid???
        case Array(u)           => (u, None)
        case Array(u, fragment) => (u, Some(fragment))
        case other              =>
          // -2 = -1 of the length diff and -1 for # char
          val str = url.substring(0, url.length - 2 - other.last.length)
          (str, Some(other.last))
      }
    } else (url, None)
  }

  def toReferences: Seq[Reference] = refs.values.toSeq
}

object EmptyReferenceCollector extends ReferenceCollector {}
