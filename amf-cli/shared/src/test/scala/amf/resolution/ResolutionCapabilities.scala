package amf.resolution

import amf.client.environment.AMFConfiguration
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipelineRunner
import amf.core.internal.remote._
import amf.core.resolution.pipelines.TransformationPipelineRunner
import amf.core.services.RuntimeResolver
import amf.plugins.document.apicontract.resolution.pipelines.{AmfEditingPipeline, AmfTransformationPipeline}

trait ResolutionCapabilities {
  protected def transform(unit: BaseUnit, pipeline: String, vendor: Vendor, amfConfig: AMFConfiguration): BaseUnit = {
    vendor match {
      case AsyncApi | AsyncApi20 | Raml08 | Raml10 | Oas20 | Oas30 =>
        amfConfig.createClient().transform(unit, PipelineName.from(vendor.name, pipeline)).bu
      case Amf    => TransformationPipelineRunner(UnhandledErrorHandler).run(unit, UnhandledAmfPipeline(pipeline))
      case target => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
  }

  object UnhandledAmfPipeline {
    def apply(pipeline: String) = pipeline match {
      case AmfEditingPipeline.name        => AmfEditingPipeline()
      case AmfTransformationPipeline.name => AmfTransformationPipeline()
      case _                              => throw new Exception(s"Cannot amf pipeline: $pipeline")
    }
  }
}
