package amf.core.parser

import amf.core.benchmark.ExecutionLog
import amf.core.exception.CyclicReferenceException
import amf.core.model.document._
import amf.core.parser
import amf.core.remote.{Cache, Context}
import amf.core.services.RuntimeCompiler
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import org.yaml.model.YNode
import amf.core.utils.Strings
import amf.core.vocabulary.Namespace

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class ReferenceResolutionResult(exception: Option[Throwable], unit: Option[BaseUnit])

case class Reference(url: String, refs: Seq[RefContainer]) extends PlatformSecrets {

  def isRemote: Boolean = !url.startsWith("#")

  def +(kind: ReferenceKind, ast: YNode, fragment: Option[String]): Reference = {
    copy(refs = refs :+ RefContainer(kind, ast, fragment))
  }

  def resolve(base: Context,
              cache: Cache,
              ctx: ParserContext,
              env: Environment,
              nodes: Seq[YNode],
              allowRecursiveRefs: Boolean): Future[ReferenceResolutionResult] = {
    val kinds = refs.map(_.linkType)
    val kind  = if (kinds.distinct.size > 1) UnspecifiedReference else kinds.distinct.head
    try {
      val res: Future[Future[ReferenceResolutionResult]] = RuntimeCompiler(url,
                                                                           None,
                                                                           None,
                                                                           base,
                                                                           kind,
                                                                           cache,
                                                                           Some(ctx),
                                                                           env) map { eventualUnit =>
        verifyMatchingKind(eventualUnit, kind, nodes, ctx)
        Future(parser.ReferenceResolutionResult(None, Some(eventualUnit)))
      } recover {
        case e: CyclicReferenceException if allowRecursiveRefs =>
          val fulllUrl = e.history.last
          resolveRecursiveUnit(fulllUrl).map(u => ReferenceResolutionResult(None, Some(u)))
        case e: Throwable =>
          Future(ReferenceResolutionResult(Some(e), None))
      }
      res flatMap identity
    } catch {
      case e: Throwable =>
        Future(ReferenceResolutionResult(Some(e), None))
    }
  }

  protected def resolveRecursiveUnit(fulllUrl: String): Future[RecursiveUnit] = {
    ExecutionLog.log(s"AMFCompiler#parserReferences: Recursive reference $fulllUrl")
    platform.resolve(fulllUrl, Environment()) map { content =>
      val recUnit = RecursiveUnit().withId(fulllUrl).withLocation(fulllUrl)
      recUnit.withRaw(content.stream.toString)
      recUnit
    }
  }

  def isInferred: Boolean = refs.exists(_.linkType == InferredLinkReference)

  private def verifyMatchingKind(unit: BaseUnit,
                                 kind: ReferenceKind,
                                 nodes: Seq[YNode],
                                 ctx: ParserContext): BaseUnit = {
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
