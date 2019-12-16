package amf.validation

import amf.{Async20Profile, ProfileName}
import amf.core.remote.{AsyncYamlHint, Hint}
import org.scalatest.Assertion

import scala.concurrent.Future

class ValidAsyncModelParserTest extends ValidModelTest {

//  test("Test test") {
//    checkValid("someapi.yaml")
//  }

  override protected def checkValid(api: String, profile: ProfileName = Async20Profile): Future[Assertion] =
    super.checkValid(api, profile)

  override val basePath: String = "file://amf-client/shared/src/test/resources/validations/async/"
  override val hint: Hint       = AsyncYamlHint
}
