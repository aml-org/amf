package amf.configuration

import amf.aml.client.scala.AMLConfiguration
import amf.apicontract.client.scala.APIConfiguration
import amf.core.internal.remote.Spec.AML
import amf.core.internal.remote.{AmlDialectSpec, Spec}
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class E2EAMLRelatedConfigurationSetupTest extends AsyncFunSuite with Matchers {

  private val baseConfig = AMLConfiguration.predefined()
  private val base       = "file://amf-cli/shared/src/test/resources/configuration/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("AML Dialect Instance belongs to spec defined by Dialect name and version") {
    baseConfig.withDialect(s"${base}dialect.yaml").flatMap { config =>
      checkValidAndSourceVendor("instance.yaml", AmlDialectSpec("Movie 1.0"), config)
    }
  }

  test("Dialect has AML SourceVendor") {
    checkValidAndSourceVendor("dialect.yaml", AML)
  }

  test("DialectFragment has AML SourceVendor") {
    checkValidAndSourceVendor("dialect-library.yaml", AML)
  }

  test("DialectLibrary has AML SourceVendor") {
    checkValidAndSourceVendor("dialect-fragment.yaml", AML)
  }

  test("Vocabulary has AML SourceVendor") {
    checkValidAndSourceVendor("vocabulary.yaml", AML)
  }

  test("API Configuration can validate dialect instance") {
    val config = APIConfiguration.API()
    config
      .withDialect(s"${base}dialect.yaml")
      .flatMap { config =>
        config.baseUnitClient().parse(base + "instance.yaml")
      }
      .flatMap { result =>
        config.baseUnitClient().validate(result.baseUnit)
      }
      .map { result =>
        result.conforms shouldBe true
      }
  }

  private def checkValidAndSourceVendor(
      file: String,
      spec: Spec,
      config: AMLConfiguration = baseConfig
  ): Future[Assertion] = {
    config.baseUnitClient().parse(base + file).map { result =>
      result.results should have length 0
      result.baseUnit.sourceSpec shouldEqual Some(spec)
    }
  }
}
