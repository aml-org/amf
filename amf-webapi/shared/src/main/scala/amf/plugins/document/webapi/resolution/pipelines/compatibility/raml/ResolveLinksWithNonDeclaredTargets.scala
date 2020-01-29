package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{Shape, DomainElement, Linkable}
import amf.core.resolution.stages.ResolutionStage
import amf.core.resolution.stages.elements.resolution.ReferenceResolution.ASSERT_DIFFERENT
import amf.core.resolution.stages.elements.resolution.ReferenceResolution
import amf.plugins.domain.shapes.models.{NodeShape, ArrayShape, AnyShape}
import scala.reflect.ClassTag

import scala.collection.mutable.ListBuffer

// TODO we need to do this because some links might point to properties within declared elements
class ResolveLinksWithNonDeclaredTargets()(
    override implicit val errorHandler: ErrorHandler
) extends ResolutionStage {

  case class Helper(private val selector: Linkable => Boolean) {
    private val resolver = new ReferenceResolution(errorHandler)

    def tryResolve(element: DomainElement): Option[DomainElement] = {
      element match {
        case l: Linkable if l.isLink && selector(l) =>
          resolver.transform(l, Seq(ASSERT_DIFFERENT)) match {
            case Some(resolved) => Some(resolved)
            case None           => None
          }
        case _ => None
      }
    }

    def resolve[T](
        element: DomainElement
    )(consumer: T => Unit)(implicit tag: ClassTag[T]): Unit = {
      tryResolve(element) match {
        case Some(resolved: T) => consumer(resolved.asInstanceOf[T])
        case _                 => // Nothing
      }
    }
  }

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case d: Document =>
        val selector = nonDeclarationLinksSelector(d.declares.map(_.id))
        val helper   = Helper(selector)
        d.iterator().foreach {
          case node: NodeShape =>
            node.properties.foreach { p =>
              helper.resolve[Shape](p.range)(p.withRange)
            }
          case array: ArrayShape =>
            helper.resolve[Shape](array.items)(array.withItems)
          case any: AnyShape =>
            val newAnd = any.and.map { s =>
              helper.tryResolve(s) match {
                case Some(resolved: Shape) => resolved
                case _                     => s
              }
            }
            any.withAnd(newAnd)
          case s: Shape if d.declares.contains(s) =>
            helper.resolve[DomainElement](s) { resolved =>
              val newDeclares = d.declares.filter(e => e != s) :+ resolved
              d.withDeclares(newDeclares)
            }
          case _ => // Nothing
        }
      case _ => // Nothing
    }
    model
  }

  def handleNodeShape(node: NodeShape, helper: Helper): Unit = {}

  def nonDeclarationLinksSelector(
      declarations: Seq[String]
  ): Linkable => Boolean = { l =>
    val targetId = l.effectiveLinkTarget().id
    l.id != targetId && !declarations.contains(targetId)
  }
}
