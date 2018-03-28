package amf.plugins.document.webapi.resolution.pipelines

import amf.core.AMFCompilerRunCount
import amf.core.annotations.LexicalInformation
import amf.core.benchmark.ExecutionLog
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.{ErrorHandler, ParserContext}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.InheritanceIncompatibleShapeError
import amf.plugins.features.validation.ParserSideValidations

class ValidatioShapeNormalisationStage(profile: String, override val keepEditingInfo: Boolean, val parserCount: Int, val currentFile: String) extends ShapeNormalizationStage(profile, keepEditingInfo) with ErrorHandler {

  override protected def minShape(baseShapeOrig: Shape, superShape: Shape): Shape = {
    try {
      super.minShape(baseShapeOrig, superShape)
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        violation(
          ParserSideValidations.InvalidTypeInheritanceErrorSpecification.id(),
          baseShapeOrig.id,
          Some(ShapeModel.Inherits.value.iri()),
          e.getMessage,
          baseShapeOrig.annotations.find(classOf[LexicalInformation])
        )
        baseShapeOrig
      case other: Exception => throw other
    }
  }
}

class ValidationResolutionPipeline(profile: String) extends ResolutionPipeline {

  val references = new ReferenceResolutionStage(profile, keepEditingInfo = false)
  val extensions = new ExtensionsResolutionStage(profile, keepEditingInfo = false)

  override def resolve[T <: BaseUnit](model: T): T = {
    // this can get not set if the model has been created manually without parsing
    val parserRun = model.parserRun match {
      case Some(run) => run
      case None      =>
        model.parserRun = Some(AMFCompilerRunCount.nextRun())
        model.parserRun.get
    }

    val shapes  = new ValidatioShapeNormalisationStage(profile, keepEditingInfo = false, parserRun, model.location)

    ExecutionLog.log(s"ValidationResolutionPipeline#resolve: resolving ${model.location}")
    withModel(model) { () =>
      step(references)
      step(extensions)
      step(shapes)
      ExecutionLog.log(s"ValidationResolutionPipeline#resolve: resolution finished ${model.location}")
    }
  }

}
