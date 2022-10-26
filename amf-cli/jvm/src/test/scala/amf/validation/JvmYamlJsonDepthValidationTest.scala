package amf.validation

import amf.core.client.common.validation.StrictValidationMode
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.model.domain.ScalarShape

class JvmYamlJsonDepthValidationTest extends YamlJsonDepthValidationTest {

  test("payload validation JSON") {

    val validator =
      oasConfig.elementClient().payloadValidatorFor(ScalarShape(), `application/json`, StrictValidationMode)

    val report = validator.validate(nestedDepthJSON)

    report.map { r =>
      // In JVM the validation of JSONTokenerHack of AMF is run first here, so the message is not the same of JS
      assertThresholdViolation(r.results, s"Reached maximum nesting value of $limit in JSON")
    }
  }

}
