package amf.resolution

import amf.shapes.client.scala.model.domain.NodeShape
import amf.testing.AMFModelTest
import amf.testing.BaseUnitUtils._

class GrpcResolutionTest extends AMFModelTest {
  val basePath: String = "file://amf-cli/shared/src/test/resources/validations/grpc/"

  test("nested reference in messages in the same file") {
    val api = s"$basePath/references/inner-ref.proto"
    grpcClient.parse(api) flatMap { parseResult =>
      println(parseResult)
      parseResult.conforms shouldBe true
      val bu                = parseResult.baseUnit
      val parseDeclarations = getDeclarations(bu)
      val person            = parseDeclarations.last.asInstanceOf[NodeShape]
      val personAddress     = person.properties.last.range
      personAddress.isLink shouldBe true

      val transformResult = grpcClient.transform(bu)
      transformResult.conforms shouldBe true
      val transformDeclarations = getDeclarations(transformResult.baseUnit)
      val personResolved        = transformDeclarations.last.asInstanceOf[NodeShape]
      val personAddressResolved = personResolved.properties.last.range
      personAddressResolved.isLink shouldBe false
    }
  }

  test("nested reference in message in a library that imports another library") {
    val api = s"$basePath/references/nested-ref.proto"
    grpcClient.parse(api) flatMap { parseResult =>
      parseResult.conforms shouldBe true
      val bu                = parseResult.baseUnit
      val parseDeclarations = getDeclarations(bu)
      val helloRequest      = parseDeclarations.head.asInstanceOf[NodeShape]
      val messageAB         = helloRequest.properties.head.range
      messageAB.isLink shouldBe true

      val transformResult = grpcClient.transform(bu)
      transformResult.conforms shouldBe true
      val transformDeclarations = getDeclarations(transformResult.baseUnit)
      val helloRequestResolved  = transformDeclarations.head.asInstanceOf[NodeShape]
      val messageABResolved     = helloRequestResolved.properties.head.range
      messageABResolved.isLink shouldBe false
    }
  }

  test("nested references in messages in multiple files/libraries") {
    val api = s"$basePath/references/outer-ref.proto"
    grpcClient.parse(api) flatMap { parseResult =>
      parseResult.conforms shouldBe true
      val bu                = parseResult.baseUnit
      val parseDeclarations = getDeclarations(bu)
      val helloRequest      = parseDeclarations.head.asInstanceOf[NodeShape]
      val messageA          = helloRequest.properties.head.range
      val messageB          = helloRequest.properties.last.range
      messageA.isLink shouldBe true
      messageB.isLink shouldBe true

      val transformResult       = grpcClient.transform(bu)
      transformResult.conforms shouldBe true
      val transformDeclarations = getDeclarations(transformResult.baseUnit)
      val helloRequestResolved  = transformDeclarations.head.asInstanceOf[NodeShape]
      val messageAResolved      = helloRequestResolved.properties.head.range
      val messageBResolved      = helloRequestResolved.properties.last.range
      messageAResolved.isLink shouldBe false
      messageBResolved.isLink shouldBe false
    }
  }
}
