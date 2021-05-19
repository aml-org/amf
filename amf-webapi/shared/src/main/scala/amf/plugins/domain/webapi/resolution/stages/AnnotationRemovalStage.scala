package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.{BaseUnit, Document, FieldsFilter}
import amf.core.model.domain.Annotation
import amf.core.resolution.stages.TransformationStep
import amf.core.traversal.iterator.{DomainElementIterator, IdCollector, InstanceCollector}
import amf.plugins.document.webapi.annotations.{ExternalJsonSchemaShape, ExternalReferenceUrl}

class AnnotationRemovalStage() extends TransformationStep() {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case d: Document =>
      d.iterator(fieldsFilter = FieldsFilter.All, visited = InstanceCollector())
        .foreach(_.annotations.reject(eliminationCriteria))
      model
    case _ => model
  }

  val removalList = List(classOf[ExternalReferenceUrl], classOf[ExternalJsonSchemaShape])

  def eliminationCriteria(a: Annotation): Boolean = removalList.exists(_.isInstance(a))

}
