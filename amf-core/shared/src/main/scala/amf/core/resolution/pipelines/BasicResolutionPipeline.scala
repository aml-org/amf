package amf.core.resolution.pipelines

import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.{AmfProfile, ProfileName}

class BasicResolutionPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {
  val references                           = new ReferenceResolutionStage(keepEditingInfo = false)(errorHandler)
  override val steps: Seq[ResolutionStage] = Seq(references)
  override def profileName: ProfileName    = AmfProfile
}
