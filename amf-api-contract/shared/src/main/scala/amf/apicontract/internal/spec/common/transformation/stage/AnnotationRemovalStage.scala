package amf.apicontract.internal.spec.common.transformation.stage

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, FieldsFilter}
import amf.core.client.scala.model.domain.Annotation
import amf.core.client.scala.transform.TransformationStep
import amf.core.client.scala.traversal.iterator.InstanceCollector
import amf.shapes.internal.annotations.{ExternalJsonSchemaShape, ExternalReferenceUrl}

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
