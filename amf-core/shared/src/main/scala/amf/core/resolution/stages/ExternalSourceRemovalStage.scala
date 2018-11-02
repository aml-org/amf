package amf.core.resolution.stages

import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DomainElement, ExternalSourceElement}
import amf.core.parser.ErrorHandler

import scala.collection.mutable

class ExternalSourceRemovalStage(val visited: mutable.Set[String] = mutable.Set())(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = model.transform(selector, transformation).asInstanceOf[T]

  private def selector(element: DomainElement): Boolean = {
    if (visited.contains(element.id)) true
    else {
      visited += element.id
      element match {
        case ex: ExternalSourceElement if ex.fields.exists(ExternalSourceElementModel.ReferenceId) =>
          true
        case _ => false
      }
    }
  }

  private def transformation(element: DomainElement, cycle: Boolean): Option[DomainElement] = {
    element match {
      case ex: ExternalSourceElement =>
        ex.fields.removeField(ExternalSourceElementModel.ReferenceId)
        Some(ex)
      case _ => Some(element)
    }
  }
}
