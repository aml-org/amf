package amf.cycle

import amf.core.remote.{AmfJsonHint, AsyncJsonHint, AsyncYamlHint, Hint}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/upanddown/cycle/async20/yaml]]
  *   origin: AsyncApi20
  *   target: Amf
  */
class Async20YamlCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = AsyncYamlHint
  override def target: Hint          = AmfJsonHint
  override def fileExtension: String = ".yaml"

  override def basePath: String =
    "amf-client/shared/src/test/resources/upanddown/cycle/async20/yaml/"
}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/upanddown/cycle/async20/json]]
  *   origin: AsyncApi20
  *   target: Amf
  */
class Async20JsonCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = AsyncJsonHint
  override def target: Hint          = AmfJsonHint
  override def fileExtension: String = ".json"

  override def basePath: String =
    "amf-client/shared/src/test/resources/upanddown/cycle/async20/json/"
}
