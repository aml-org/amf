package amf.core.resolution.stages

import amf.core.model.document.BaseUnit

abstract class ResolutionStage(profile: String) {
    def resolve(model: BaseUnit): BaseUnit
}
