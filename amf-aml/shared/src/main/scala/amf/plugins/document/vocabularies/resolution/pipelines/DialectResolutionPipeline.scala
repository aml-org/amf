package amf.plugins.document.vocabularies.resolution.pipelines

import amf.core.parser.ErrorHandler
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.resolution.stages.DialectReferencesResolutionStage
import amf.{AmfProfile, ProfileName}

class DialectResolutionPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  override protected val steps: Seq[ResolutionStage] = Seq(new DialectReferencesResolutionStage())
  override def profileName: ProfileName              = AmfProfile
}
