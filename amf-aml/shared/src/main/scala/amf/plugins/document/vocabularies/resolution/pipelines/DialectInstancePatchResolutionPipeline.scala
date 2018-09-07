package amf.plugins.document.vocabularies.resolution.pipelines
import amf.{AmfProfile, ProfileName}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{CleanReferencesStage, DeclarationsRemovalStage, ResolutionStage}
import amf.plugins.document.vocabularies.model.document.DialectInstancePatch
import amf.plugins.document.vocabularies.resolution.stages.{DialectInstanceReferencesResolutionStage, DialectPatchApplicationStage}

class DialectInstancePatchResolutionPipeline(override val model: DialectInstancePatch) extends ResolutionPipeline[DialectInstancePatch]() {

  override protected val steps: Seq[ResolutionStage] = Seq(
    new DialectInstanceReferencesResolutionStage(),
    new DialectPatchApplicationStage(),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage()
  )

  override def profileName: ProfileName = AmfProfile

}
