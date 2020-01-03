package amf.emit

import amf.core.remote.{Amf, AsyncYamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.WebApi

class Async20CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/async20/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cyclesAsyncAmf: Seq[FixtureData] = Seq(
    FixtureData("Simple publish and subscribe", "publish-subscribe.yaml", "publish-subscribe.jsonld"),
    FixtureData("Message object", "message-obj.yaml", "message-obj.jsonld"),
    FixtureData("Draft 7 schemas", "draft-7-schemas.yaml", "draft-7-schemas.jsonld"),
    FixtureData("Parameters object", "channel-parameters.yaml", "channel-parameters.jsonld"),
    FixtureData("Security schemes", "security-schemes.yaml", "security-schemes.jsonld")
  )

  cyclesAsyncAmf.foreach { f =>
    test(s"${f.name} - async to amf") {
      cycle(f.apiFrom, f.apiTo, AsyncYamlHint, Amf)
    }
  }
}
