package amf.configuration

import amf.aml.client.scala.AMLConfiguration
import amf.core.internal.remote.{AmlDialectSpec, Spec}
import amf.core.internal.remote.Spec.AML
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class E2EDialectParserConfigurationSetupTest extends AsyncFunSuite with Matchers {

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

  private def checkValidAndSourceVendor(file: String,
                                        spec: Spec,
                                        config: AMLConfiguration = baseConfig): Future[Assertion] = {
    config.baseUnitClient().parse(base + file).map { result =>
      result.results should have length 0
      result.baseUnit.sourceSpec shouldEqual Some(spec)
    }
  }
}
