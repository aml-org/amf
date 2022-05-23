package amf.validation

import amf.apicontract.client.scala.RAMLConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.StrictValidationMode
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import scala.concurrent.ExecutionContext

class InvestigationTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath                                             = "file://amf-cli/shared/src/test/resources"
  val api                                                  = s"$basePath/sample-project/api.raml"
  val config                                               = RAMLConfiguration.RAML10()
  val client                                               = config.baseUnitClient()

  def getRequestSchema(unit: BaseUnit): Shape = {
    unit
      .asInstanceOf[Document]
      .encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head
      .request
      .payloads
      .head
      .schema
  }

  test("Correct payload") {
    client.parse(api) flatMap { parseResult =>
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val schema          = getRequestSchema(transformResult.baseUnit)
      val validator =
        config.elementClient().payloadValidatorFor(schema, "application/json", StrictValidationMode)
      validator
        .validate(
          "{\n\"employee\": [\n{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"Temporary\"\n\t},\n\t{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"Permenant\"\n\t}\n]\n}"
        )
        .flatMap { validationResult =>
          validationResult.conforms shouldBe true
        }
    }
  }

  test("Invalid employeeType for first element") {
    client.parse(api) flatMap { parseResult =>
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val schema          = getRequestSchema(transformResult.baseUnit)
      val validator =
        config.elementClient().payloadValidatorFor(schema, "application/json", StrictValidationMode)
      validator
        .validate(
          "{\n\"employee\": [\n{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"WrongType\"\n\t},\n\t{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"Permenant\"\n\t}\n]\n}"
        )
        .flatMap { validationResult =>
          validationResult.conforms shouldBe false
        }
    }
  }

  test("Invalid employeeType for second element") {
    client.parse(api) flatMap { parseResult =>
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val schema          = getRequestSchema(transformResult.baseUnit)
      val validator =
        config.elementClient().payloadValidatorFor(schema, "application/json", StrictValidationMode)
      validator
        .validate(
          "{\n\"employee\": [\n{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"Temporary\"\n\t},\n\t{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"WrongType\"\n\t}\n]\n}"
        )
        .flatMap { validationResult =>
          validationResult.conforms shouldBe false
        }
    }
  }

  test("No value for required field employeeType for second element") {
    client.parse(api) flatMap { parseResult =>
      val transformResult = client.transform(parseResult.baseUnit, PipelineId.Editing)
      val schema          = getRequestSchema(transformResult.baseUnit)
      val validator =
        config.elementClient().payloadValidatorFor(schema, "application/json", StrictValidationMode)
      validator
        .validate(
          "{\n\"employee\": [\n{\n\t\"employeeId\": \"123\",\n\t\"employeeType\": \"Temporary\"\n\t},\n\t{\n\t\"employeeId\": \"123\"\n\t}\n]\n}"
        )
        .flatMap { validationResult =>
          validationResult.conforms shouldBe false
        }
    }
  }
}
