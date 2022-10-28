package amf.validation

import amf.core.client.common.validation.StrictValidationMode
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.model.domain.ScalarShape

class JsYamlJsonDepthValidationTest extends YamlJsonDepthValidationTest {

  test("payload validation JSON") {

    val validator =
      oasConfig.elementClient().payloadValidatorFor(ScalarShape(), `application/json`, StrictValidationMode)

    val report = validator.validate(nestedDepthJSON)

    report.map { r =>
      // In JS the payload is directly processed by the JSON Schema library. It throw a JavaScriptException with a syntax error and it is wrapped by AMF
      assertThresholdViolation(r.results, s"Unsupported chars in string value (probably a binary file)")
    }
  }

}
