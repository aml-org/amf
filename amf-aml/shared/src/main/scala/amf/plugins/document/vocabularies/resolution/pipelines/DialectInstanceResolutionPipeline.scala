package amf.plugins.document.vocabularies.resolution.pipelines

import amf.core.parser.{DefaultParserSideErrorHandler, ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{CleanReferencesStage, DeclarationsRemovalStage, ResolutionStage}
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.document.vocabularies.resolution.stages.DialectInstanceReferencesResolutionStage
import amf.{AmfProfile, ProfileName}

class DialectInstanceResolutionPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = Seq(
    new DialectInstanceReferencesResolutionStage(),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage()
  )
  override def profileName: ProfileName = AmfProfile
}
