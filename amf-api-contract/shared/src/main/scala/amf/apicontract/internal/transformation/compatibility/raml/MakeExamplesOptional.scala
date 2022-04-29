package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.Example

class MakeExamplesOptional() extends TransformationStep {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    try {
      model.iterator().foreach {
        case example: Example =>
          makeOptional(example)
        case _ => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want this to break anything
    }
    model
  }

  def makeOptional(example: Example): Unit = {
    example.withStrict(false)
  }

}
