package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.internal.transformation.{AmfEditingPipeline, AmfTransformationPipeline}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.internal.remote._

trait ResolutionCapabilities {
  protected def transform(unit: BaseUnit, pipeline: String, spec: Spec, amfConfig: AMFConfiguration): BaseUnit = {
    spec match {
      case AsyncApi | AsyncApi20 | Raml08 | Raml10 | Oas20 | Oas30 =>
        amfConfig.baseUnitClient().transform(unit, pipeline).baseUnit
      case Amf =>
        TransformationPipelineRunner(UnhandledErrorHandler, amfConfig).run(unit, UnhandledAmfPipeline(pipeline))
      case target => throw new Exception(s"Cannot resolve $target")
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
