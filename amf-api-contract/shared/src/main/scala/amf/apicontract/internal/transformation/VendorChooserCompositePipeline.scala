package amf.apicontract.internal.transformation

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.{TransformationPipeline, TransformationPipelineRunner, TransformationStep}
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations.ResolutionValidation

case class VendorChooserCompositePipeline private[amf] (name: String, pipelines: Map[String, TransformationPipeline])
    extends TransformationPipeline {

  override def steps: Seq[TransformationStep] = Seq(
    VendorChooserTransformationStep(name, pipelines)
  )

  def add(vendor: Spec, pipeline: TransformationPipeline) = copy(pipelines = pipelines + (vendor.id -> pipeline))
  def add(tuple: (String, TransformationPipeline))        = copy(pipelines = pipelines + tuple)
  def add(tuples: Seq[(String, TransformationPipeline)])  = copy(pipelines = pipelines ++ tuples.toMap)
}

object VendorChooserCompositePipeline {
  def apply(name: String) = new VendorChooserCompositePipeline(name, Map.empty)
}

case class VendorChooserTransformationStep(name: String, pipelines: Map[String, TransformationPipeline])
    extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model.sourceVendor
      .flatMap(vendor => pipelines.get(vendor.id))
      .map(pipeline => TransformationPipelineRunner(errorHandler).run(model, pipeline)) match {
      case None =>
        errorHandler.violation(
          ResolutionValidation,
          model.id,
          None,
          getErrorMessage(model),
          model.position(),
          model.location()
        )
        model
      case Some(next) => next
    }
  }

  private def getErrorMessage(model: BaseUnit): String = {
    model.sourceVendor
      .map(vendor => s"Cannot find transformation pipeline with name $name and spec ${vendor.id}")
      .getOrElse(s"Cannot decide which pipeline to use when BaseUnit doesn't have the spec defined")
  }
}
