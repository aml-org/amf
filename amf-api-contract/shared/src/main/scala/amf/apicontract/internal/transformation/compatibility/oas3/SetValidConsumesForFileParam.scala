package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.FileShape
import org.mulesoft.common.collections.FilterType

class SetValidConsumesForFileParam() extends TransformationStep {
  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
    val operations = model.iterator().toList.filterType[Operation]
    operations.foreach(modifyConsumesIfFileIsPresent)
    model
  }

  val validConsumes = List("multipart/form-data", "application/x-www-form-urlencoded")

  def modifyConsumesIfFileIsPresent(o: Operation): Unit = {
    val payloads = o.requests.flatMap(_.payloads)
    val fileShapePresent = payloads.exists { p =>
      Option(p.schema).exists(_.isInstanceOf[FileShape])
    }
    if(fileShapePresent)
      o.withAccepts(validConsumes)
  }

}
