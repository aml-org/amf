package amf.framework.resolution.stages

import amf.framework.model.document.BaseUnit

abstract class ResolutionStage(profile: String) {
    def resolve(model: BaseUnit): BaseUnit
}
