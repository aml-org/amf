package amf.resolution

import amf.document.BaseUnit

abstract class ResolutionStage(profile: String) {
    def resolve(model: BaseUnit, context: Any): BaseUnit
}
