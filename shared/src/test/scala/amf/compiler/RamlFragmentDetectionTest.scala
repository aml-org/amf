package amf.compiler

import amf.remote.RamlYamlHint
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class RamlFragmentDetectionTest extends AsyncFunSuite with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://shared/src/test/resources/references/fragments/"
  test("Detect Raml Data Type Fragment") {
    assertHeader("data-type-without-header.raml", Some(RamlFragmentHeader.Raml10DataType))
  }

  test("Detect Raml ResourceType") {
    assertHeader("resource-type-without-header.raml", Some(RamlFragmentHeader.Raml10ResourceType))
  }

  test("Detect Raml Trait") {
    assertHeader("trait-without-header.raml", Some(RamlFragmentHeader.Raml10Trait))
  }

  test("Detect Raml DocumentationItem") {
    assertHeader("documentation-item-without-header.raml", Some(RamlFragmentHeader.Raml10DocumentationItem))
  }

  test("Detect Raml Annotation Type Declaration") {
    assertHeader("annotation-without-header.raml", Some(RamlFragmentHeader.Raml10AnnotationTypeDeclaration))
  }

  test("Detect Raml Any matching fragment") {
    assertHeader("no-match-without-header.raml", None)
  }

  test("Detect Raml More than one matching fragment") {
    assertHeader("two-match-without-header.raml", None)
  }

  test("Detect Raml SecurityScheme") {
    assertHeader("security-scheme.raml", Some(RamlFragmentHeader.Raml10SecurityScheme))
  }

  test("Detect Raml SecurityScheme without header") {
    assertHeader("security-scheme-without-header.raml", Some(RamlFragmentHeader.Raml10SecurityScheme))
  }

  private def assertHeader(path: String, expectedOption: Option[RamlHeader]): Future[Assertion] = {
    AMFCompiler(basePath + path, platform, RamlYamlHint, Validation(platform))
      .root()
      .map(RamlHeader(_) shouldBe expectedOption)
  }

}
