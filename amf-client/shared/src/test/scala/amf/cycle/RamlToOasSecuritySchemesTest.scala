package amf.cycle

import amf.core.remote.{OasJsonHint, RamlYamlHint, Vendor}
import amf.io.FunSuiteCycleTests

class RamlToOasSecuritySchemesTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-client/shared/src/test/resources/extensions/security-schemes/"

  test("Raml oauth 1.0 scheme in OAS 2.0") {
    cycle("oauth1.raml", "cycles/oauth1.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml oauth 1.0 scheme in OAS 3.0") {
    cycle("oauth1.raml", "cycles/oauth1.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("Raml oauth 2.0 scheme in OAS 2.0") {
    cycle("oauth2.raml", "cycles/oauth2.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml oauth 2.0 scheme in OAS 3.0") {
    cycle("oauth2.raml", "cycles/oauth2.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("Raml basic auth scheme in OAS 2.0") {
    cycle("basicAuth.raml", "cycles/basicAuth.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml basic auth scheme in OAS 3.0") {
    cycle("basicAuth.raml", "cycles/basicAuth.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("Raml digest auth scheme in OAS 2.0") {
    cycle("digestAuth.raml", "cycles/digestAuth.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml digest auth scheme in OAS 3.0") {
    cycle("digestAuth.raml", "cycles/digestAuth.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("Raml pass through scheme in OAS 2.0") {
    cycle("passThrough.raml", "cycles/passThrough.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml pass through scheme in OAS 3.0") {
    cycle("passThrough.raml", "cycles/passThrough.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("Raml custom scheme in OAS 2.0") {
    cycle("customSecurity.raml", "cycles/customSecurity.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("Raml custom scheme in OAS 3.0") {
    cycle("customSecurity.raml", "cycles/customSecurity.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("OAS 2.0 oauth 1.0 scheme in RAML") {
    cycle("cycles/oauth1.oas2.json", "cycles/oauth1.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 oauth 1.0 scheme in RAML") {
    cycle("cycles/oauth1.oas3.json", "cycles/oauth1.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 oauth 2.0 scheme in RAML") {
    cycle("cycles/oauth2.oas2.json", "cycles/oauth2.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 oauth 2.0 scheme in RAML") {
    cycle("cycles/oauth2.oas3.json", "cycles/oauth2.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 basic auth scheme in RAML") {
    cycle("cycles/basicAuth.oas2.json", "cycles/basicAuth.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 basic auth scheme in RAML") {
    cycle("cycles/basicAuth.oas3.json", "cycles/basicAuth.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 digest auth scheme in RAML") {
    cycle("cycles/digestAuth.oas2.json", "cycles/digestAuth.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 digest auth scheme in RAML") {
    cycle("cycles/digestAuth.oas3.json", "cycles/digestAuth.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 pass through scheme in RAML") {
    cycle("cycles/passThrough.oas2.json", "cycles/passThrough.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 pass through scheme in RAML") {
    cycle("cycles/passThrough.oas3.json", "cycles/passThrough.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 custom scheme in RAML") {
    cycle("cycles/customSecurity.oas2.json", "cycles/customSecurity.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 custom scheme in RAML") {
    cycle("cycles/customSecurity.oas3.json", "cycles/customSecurity.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 2.0 in WebAPI") {
    cycle("securedBy-webapi.raml", "cycles/securedBy-webapi.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 2.0 in Endpoint") {
    cycle("securedBy-endpoint.raml", "cycles/securedBy-endpoint.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 2.0 in Operation") {
    cycle("securedBy-operation.raml", "cycles/securedBy-operation.oas2.json", RamlYamlHint, Vendor.OAS20)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 3.0 in WebAPI") {
    cycle("securedBy-webapi.raml", "cycles/securedBy-webapi.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 3.0 in Endpoint") {
    cycle("securedBy-endpoint.raml", "cycles/securedBy-endpoint.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("RAML specific securiy schemes in securedBy are omitted in OAS 3.0 in Operation") {
    cycle("securedBy-operation.raml", "cycles/securedBy-operation.oas3.json", RamlYamlHint, Vendor.OAS30)
  }

  test("OAS 2.0 specific securiy schemes in securedBy are omitted in RAML in WebAPI") {
    cycle("cycles/securedBy-webapi.oas2.json", "cycles/securedBy-webapi.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 specific securiy schemes in securedBy are omitted in RAML in Endpoint") {
    cycle("cycles/securedBy-endpoint.oas2.json", "cycles/securedBy-endpoint.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 2.0 specific securiy schemes in securedBy are omitted in RAML in Operation") {
    cycle("cycles/securedBy-operation.oas2.json", "cycles/securedBy-operation.oas2.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 specific securiy schemes in securedBy are omitted in RAML in WebAPI") {
    cycle("cycles/securedBy-webapi.oas3.json", "cycles/securedBy-webapi.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 specific securiy schemes in securedBy are omitted in RAML in Endpoint") {
    cycle("cycles/securedBy-endpoint.oas3.json", "cycles/securedBy-endpoint.oas3.raml", OasJsonHint, Vendor.RAML10)
  }

  test("OAS 3.0 specific securiy schemes in securedBy are omitted in RAML in Operation") {
    cycle("cycles/securedBy-operation.oas3.json", "cycles/securedBy-operation.oas3.raml", OasJsonHint, Vendor.RAML10)
  }
}
