package amf.resolution.stages

import amf.document.BaseUnit

abstract class ResolutionStage(profile: String) {
    def resolve(model: BaseUnit): BaseUnit
}
