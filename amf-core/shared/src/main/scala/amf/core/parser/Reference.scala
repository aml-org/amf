package amf.core.parser

import amf.core.model.document._
import amf.core.remote.{Cache, Context}
import amf.core.services.RuntimeCompiler
import amf.internal.environment.Environment
import org.yaml.model.YNode
import amf.core.utils.Strings
import amf.core.vocabulary.Namespace

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
              env: Environment,
              nodes: Seq[YNode]): Future[BaseUnit] = {
    val kinds        = refs.map(_.linkType)
    val kind         = if (kinds.distinct.size > 1) UnspecifiedReference else kinds.distinct.head
    val eventualUnit = RuntimeCompiler(url, mediaType, vendor, base, kind, cache, Some(ctx), env)
    verifyMatchingKind(eventualUnit, kind, nodes, ctx)
  }

  def isInferred: Boolean = refs.exists(_.linkType == InferredLinkReference)

  private def verifyMatchingKind(eventualUnit: Future[BaseUnit],
                                 kind: ReferenceKind,
                                 nodes: Seq[YNode],
                                 ctx: ParserContext): Future[BaseUnit] = {
    eventualUnit.map(unit => {
      unit match {
        case _: Module => // if is a library, kind should be LibraryReference
          if (!LibraryReference.eq(kind))
            nodes.foreach(ctx.violation("Libraries must be applied by using 'uses'", _))
        // ToDo find a better way to skip vocabulary/dialect elements of this validation
        case _ if !unit.meta.`type`.exists(_.iri().contains(Namespace.Meta.base)) =>
          // if is not a library, and is not a vocabulary, kind should not be LibraryReference
          if (LibraryReference.eq(kind))
            nodes.foreach(ctx.violation("Fragments must be imported by using '!include'", _))
        case _ => // nothing to do
      }
      unit
    })
  }
}

object Reference {
  def apply(url: String, kind: ReferenceKind, node: YNode, fragment: Option[String]): Reference =
    new Reference(url, Seq(RefContainer(kind, node, fragment)))
}

case class RefContainer(linkType: ReferenceKind, node: YNode, fragment: Option[String])

case class ReferenceCollector() {
  private val refs = mutable.Map[String, Reference]()

  def +=(key: String, kind: ReferenceKind, node: YNode): Unit = {
    val (url, fragment) = ReferenceFragmentPartition(key)
    refs.get(url) match {
      case Some(reference: Reference) => refs.update(url, reference + (kind, node, fragment))
      case None                       => refs += url -> Reference(url, kind, node, fragment)
    }
  }

  def toReferences: Seq[Reference] = refs.values.toSeq
}

object EmptyReferenceCollector extends ReferenceCollector {}

object ReferenceFragmentPartition {
  def apply(url: String): (String, Option[String]) = {
    if (url.normalizeUrl.startsWith("file://") && !url.startsWith("#")) { // http urls supports # refs
      url.split("#") match { // how can i know if the # its part of the uri or not? uri not valid???
        case Array(u) if u.endsWith("#") => (u.substring(0, u.length - 2), None)
        case Array(u)                    => (u, None)
        case Array(u, fragment)          => (u, Some(fragment))
        case other                       =>
          //  -1 of the length diff and -1 for # char
          val str = url.substring(0, url.length - 1 - other.last.length)
          (str, Some(other.last))
      }
    } else (url, None)
  }
}
