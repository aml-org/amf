package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document, FieldsFilter}
import amf.core.model.domain.Annotation
import amf.core.resolution.stages.ResolutionStage
import amf.core.traversal.iterator.{DomainElementIterator, IdCollector, InstanceCollector}
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl

class AnnotationRemovalStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case d: Document =>
      d.iterator(fieldsFilter = FieldsFilter.All, visited = InstanceCollector())
        .foreach(_.annotations.reject(eliminationCriteria))
      model
    case _ => model
  }

  val removalList = List(classOf[ExternalReferenceUrl])

  def eliminationCriteria(a: Annotation): Boolean = removalList.exists(_.isInstance(a))

}
