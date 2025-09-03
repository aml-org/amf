package amf.validation

import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.testing.AMFModelTest
import amf.testing.BaseUnitUtils._

class GrpcModelAssertionTest extends AMFModelTest {
  val basePath: String = "file://amf-cli/shared/src/test/resources/validations/grpc/"

  test("valid reserved values in message declaration") {
    val api = s"$basePath/valid-reserved-message.proto"
    grpcClient.parse(api) flatMap { parseResult =>
      parseResult.conforms shouldBe true
      val bu                = parseResult.baseUnit
      val parseDeclarations = getDeclarations(bu)
      val userMessage       = parseDeclarations.head.asInstanceOf[NodeShape]
      userMessage.reservedValues.size shouldBe 9
    }
  }

  test("valid reserved values in enum declaration") {
    val api = s"$basePath/valid-reserved-enum.proto"
    grpcClient.parse(api) flatMap { parseResult =>
      parseResult.conforms shouldBe true
      val bu                = parseResult.baseUnit
      val parseDeclarations = getDeclarations(bu)
      val accountStatusEnum = parseDeclarations.head.asInstanceOf[ScalarShape]
      accountStatusEnum.reservedValues.size shouldBe 9
    }
  }
}
