package amf.compiler

import amf.plugins.document.webapi.parser.OasHeader
import amf.remote.OasJsonHint
import amf.core.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.{Assertion, AsyncFunSuite}
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class OasFragmentDetectionTest extends AsyncFunSuite with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://shared/src/test/resources/references/fragments/"

  test("Detect Oas Data Type Fragment") {
    assertHeader("data-type-without-header.json", Some(OasHeader.Oas20DataType))
  }

  test("Detect Oas ResourceType") {
    assertHeader("resource-type-without-header.json", Some(OasHeader.Oas20ResourceType))
  }

  test("Detect Oas Trait") {
    assertHeader("trait-without-header.json", Some(OasHeader.Oas20Trait))
  }

  test("Detect Oas DocumentationItem") {
    assertHeader("documentation-item-without-header.json", Some(OasHeader.Oas20DocumentationItem))
  }

  test("Detect Oas Annotation Type Declaration") {
    assertHeader("annotation-without-header.json", Some(OasHeader.Oas20AnnotationTypeDeclaration))
  }

  test("Detect Oas Any matching fragment") {
    assertHeader("no-match-without-header.json", None)
  }

  test("Detect Oas More than one matching fragment") {
    assertHeader("two-match-without-header.json", None)
  }

  test("Detect Oas SecurityScheme") {
    assertHeader("security-scheme.json", Some(OasHeader.Oas20SecurityScheme))
  }

  test("Detect Oas SecurityScheme without header") {
    assertHeader("security-scheme-without-header.json", Some(OasHeader.Oas20SecurityScheme))
  }

  private def assertHeader(path: String, expectedOption: Option[OasHeader]): Future[Assertion] = {
    AMFCompiler(basePath + path, platform, OasJsonHint, Validation(platform))
      .root()
      .map { root: Root =>
        OasHeader.apply(root.newFormat()) shouldBe expectedOption
      }
  }

}
