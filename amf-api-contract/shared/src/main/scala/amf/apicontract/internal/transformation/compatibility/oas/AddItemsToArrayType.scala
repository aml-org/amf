package amf.apicontract.internal.transformation.compatibility.oas

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape}

class AddItemsToArrayType extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    try {
      model
        .iterator()
        .foreach({
          case array: ArrayShape => setArrayType(array)
          case _                 =>
        })
      model
    } catch {
      case _: Throwable => model
    }
  }

  private def setArrayType(array: ArrayShape): Unit = {
    Option(array.items) match {
      case None => array.withItems(AnyShape())
      case _    => // ignore
    }
  }
}
