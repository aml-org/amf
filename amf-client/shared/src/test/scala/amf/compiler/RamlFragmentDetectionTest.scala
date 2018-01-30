package amf.compiler

import amf.core.remote.RamlYamlHint
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Root, Validation}
import amf.plugins.document.webapi.parser.{RamlFragmentHeader, RamlHeader}
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class RamlFragmentDetectionTest extends AsyncFunSuite with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-client/shared/src/test/resources/references/fragments/"
  ignore("Detect Raml Data Type Fragment") {
    assertHeader("data-type-without-header.raml", Some(RamlFragmentHeader.Raml10DataType))
  }

  ignore("Detect Raml ResourceType") {
    assertHeader("resource-type-without-header.raml", Some(RamlFragmentHeader.Raml10ResourceType))
  }

  ignore("Detect Raml Trait") {
    assertHeader("trait-without-header.raml", Some(RamlFragmentHeader.Raml10Trait))
  }

  ignore("Detect Raml DocumentationItem") {
    assertHeader("documentation-item-without-header.raml", Some(RamlFragmentHeader.Raml10DocumentationItem))
  }

  ignore("Detect Raml Annotation Type Declaration") {
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

  ignore("Detect Raml SecurityScheme without header") {
    assertHeader("security-scheme-without-header.raml", Some(RamlFragmentHeader.Raml10SecurityScheme))
  }

  private def assertHeader(path: String, expectedOption: Option[RamlHeader]): Future[Assertion] = {
    Validation(platform)
      .flatMap { v =>
        AMFCompiler(basePath + path, platform, RamlYamlHint, v)
          .root()
      }
      .map { root: Root =>
        RamlHeader(root.newFormat()) shouldBe expectedOption
      }

  }

}
