package amf.apicontract.internal.transformation.compatibility.common

import amf.aml.internal.transform.steps.SemanticExtensionFlatteningStage
import amf.core.client.scala.transform.TransformationStep

trait SemanticFlattenFilter {

  protected def filterOutSemanticStage(steps: Seq[TransformationStep]): Seq[TransformationStep] = {
    steps.collect {
      case _: SemanticExtensionFlatteningStage => None
      case other                               => Some(other)
    }.flatten
  }
}
