package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3.CleanRepeatedOperationIds
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, NodeShape}
import amf.plugins.domain.webapi.models.Parameter
import amf.{OasProfile, ProfileName}

import scala.collection.mutable

class OasCompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  private val resolution = new OasResolutionPipeline(eh)

  override val steps: Seq[ResolutionStage] = resolution.steps ++ Seq(
    new LowercaseSchemes(),
    new Oas20SecuritySettingsMapper(),
    new MandatoryDocumentationUrl(),
    new MandatoryResponses(),
    new MandatoryPathParameters(),
    new CleanNullSecurity(),
    new CleanParameterExamples(),
    new CleanIdenticalExamples(),
    new CleanRepeatedOperationIds()
  )

  override def profileName: ProfileName = OasProfile
}

object OasCompatibilityPipeline {
  def unhandled = new OasCompatibilityPipeline(UnhandledErrorHandler)
}
