package amf.emit

import amf.core.internal.remote.Syntax.Json
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests
import amf.testing.{AmfJsonLd, Oas30Json, Raml10Yaml}

class Oas30CycleTest extends FunSuiteCycleTests {
  override val basePath: String = "amf-cli/shared/src/test/resources/upanddown/oas3/"

  case class FixtureData(name: String, apiFrom: String, apiTo: String)

  val cycleOas3ToRaml10: Seq[FixtureData] = Nil

  cycleOas3ToRaml10.foreach { f =>
    test(s"${f.name} - oas3 to raml10") {
      cycle(f.apiFrom, f.apiTo, Oas30JsonHint, Raml10Yaml)
    }
  }

  val cycleOas2ToOas3 = Seq(
    FixtureData("Basic servers", "basic-servers-2.json", "basic-servers-2.json.json")
  )

  cycleOas2ToOas3.foreach { f =>
    test(s"${f.name} - oas2 to oas3") {
      cycle(f.apiFrom, f.apiTo, Oas20JsonHint, Oas30Json)
    }
  }

  val cyclesOas3 = Seq(
    FixtureData("Basic servers", "basic-servers.json", "basic-servers.json"),
    FixtureData("Complex servers", "complex-servers.json", "complex-servers.json"),
    FixtureData("Basic content", "basic-content.json", "basic-content.json"),
    FixtureData("Basic encoding", "basic-encoding.json", "basic-encoding.json"),
    FixtureData("Basic request body", "basic-request-body.json", "basic-request-body.json"),
    FixtureData("Basic response headers", "basic-headers-response.json", "basic-headers-response.json"),
    FixtureData("Basic links", "basic-links.json", "basic-links.json"),
    FixtureData("Basic callbacks", "basic-callbacks.json", "basic-callbacks.json"),
    FixtureData("Basic operation", "basic-operation.json", "basic-operation.json"),
    FixtureData("Basic oas3 patch version", "basic-oas-patch.json", "basic-oas-patch-corrected.json"),
    FixtureData("Response codes with wildcard", "response-code-wildcards.json", "response-code-wildcards.json"),
    FixtureData("Basic paths object with single server",
                "basic-paths-object-with-server.json",
                "basic-paths-object-with-server.json"),
    FixtureData("Basic paths object with multiple servers",
                "basic-paths-object-with-servers.json",
                "basic-paths-object-with-servers.json"),
    FixtureData("Basic discriminator object",
                "discriminator-object/discriminator-object.json",
                "discriminator-object/output.json"),
    FixtureData("Security scheme types", "basic-security-types.json", "basic-security-types.json"),
    FixtureData("Basic parameter object",
                "basic-parameters/basic-parameters.json",
                "basic-parameters/basic-parameters-output.json"),
    FixtureData("Basic components object", "components/basic-components.json", "components/components-output.json"),
    FixtureData("One subscription with multiple callbacks",
                "one-subscription-multiple-callbacks.json",
                "one-subscription-multiple-callbacks.json"),
    FixtureData("Deprecated field in schema object", "deprecated-field.json", "deprecated-field.json"),
    FixtureData("Several security schemes of same type",
                "several-security-schemes-of-same-type.json",
                "several-security-schemes-of-same-type.json")
  )

  cyclesOas3.foreach { f =>
    test(s"${f.name} - oas3 to oas3") {
      cycle(f.apiFrom, f.apiTo, Oas30JsonHint, Oas30Json)
    }
  }

  val cyclesRamlOas3: Seq[FixtureData] = Nil

  cyclesRamlOas3.foreach { f =>
    test(s"${f.name} - raml to oas3") {
      cycle(f.apiFrom, f.apiTo, Raml10YamlHint, Oas30Json)
    }
  }

  // TODO: jajaja
  val cyclesOas3Amf: Seq[FixtureData] = Nil

  cyclesOas3Amf.foreach { f =>
    test(s"${f.name} - oas3 to amf") {
      cycle(f.apiFrom, f.apiTo, Oas30JsonHint, AmfJsonLd)
    }
  }
}
