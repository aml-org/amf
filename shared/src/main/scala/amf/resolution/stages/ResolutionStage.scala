package amf.resolution.stages

import amf.document.BaseUnit
import amf.validation.Validation

abstract class ResolutionStage(profile: String)(implicit val currentValidation: Validation) {
    def resolve(model: BaseUnit): BaseUnit
}
