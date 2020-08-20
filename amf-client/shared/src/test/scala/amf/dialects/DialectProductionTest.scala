package amf.dialects

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.facades.{AMFCompiler, Validation}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.vocabularies.AMLPlugin
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

trait DialectInstanceTester { this: FunSuiteCycleTests =>

  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath,
                            renderOptions: Option[RenderOptions] = None): Future[Assertion] = {
    for {
      v <- Validation(platform)
      _ <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, eh = UnhandledParserErrorHandler)
        .build()
      res <- cycle(source, golden, hint, target, directory, renderOptions)
    } yield {
      res
    }
  }

}

class DialectProductionTest extends FunSuiteCycleTests with DialectInstanceTester {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/production/"

  test("Can parse and generated ABOUT dialect") {
    cycle("ABOUT-dialect.yaml", "ABOUT-dialect.raml.yaml", VocabularyYamlHint, Aml, basePath + "ABOUT/")
  }

  ignore("Can parse the canonical webapi dialect") {
    cycle("canonical_webapi.yaml", "canonical_webapi.json", VocabularyYamlHint, target = Amf, "vocabularies/dialects/")
  }

  multiGoldenTest("Can parse ABOUT dialect", "ABOUT-dialect.%s") { config =>
    cycle("ABOUT-dialect.yaml",
          config.golden,
          VocabularyYamlHint,
          target = Amf,
          directory = basePath + "ABOUT/",
          renderOptions = Some(config.renderOptions))
  }

  // TODO migrate to multiGoldenTest
  test("Can parse validation dialect") {
    cycle("validation_dialect.yaml", "validation_dialect.json", VocabularyYamlHint, target = Amf)
  }

  // TODO migrate to multiGoldenTest
  test("Can parse and generate the Instagram dialect") {
    cycle("dialect.yaml", "dialect.json", VocabularyYamlHint, target = Amf, basePath + "Instagram/")
  }

  // TODO migrate to multiGoldenTest
  test("Can parse and generate the activity dialect") {
    cycle("activity.yaml", "activity.json", VocabularyYamlHint, target = Amf, basePath + "streams/")
  }

  test("Can parse validation dialect instance") {
    withDialect("validation_dialect.yaml",
                "validation_instance1.yaml",
                "validation_instance1.raml.yaml",
                VocabularyYamlHint,
                Aml)
  }

  multiGoldenTest("Can parse validation dialect cfg1 instance", "example1_instance.%s") { config =>
    withDialect(
      "example1.yaml",
      "example1_instance.yaml",
      config.golden,
      VocabularyYamlHint,
      target = Amf,
      directory = s"${basePath}cfg/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Can parse validation dialect cfg2 instance", "example2_instance.%s") { config =>
    withDialect(
      "example2.yaml",
      "example2_instance.yaml",
      config.golden,
      VocabularyYamlHint,
      target = Amf,
      directory = basePath + "cfg/",
      renderOptions = Some(config.renderOptions)
    )
  }

  multiGoldenTest("Can parse validation dialect cfg3 instance", "example3_instance.%s") { config =>
    withDialect(
      "example3.yaml",
      "example3_instance.yaml",
      config.golden,
      VocabularyYamlHint,
      target = Amf,
      directory = basePath + "cfg/",
      renderOptions = Some(config.renderOptions)
    )
  }

  test("Can parse and generate ABOUT dialect instance") {
    withDialect("ABOUT-dialect.yaml", "ABOUT.yaml", "ABOUT.yaml.yaml", VocabularyYamlHint, Aml, basePath + "ABOUT/")
  }

  test("Can parse and generate ABOUT-github dialect instance") {
    withDialect("ABOUT-GitHub-dialect.yaml",
                "example.yaml",
                "example.yaml.yaml",
                VocabularyYamlHint,
                Aml,
                basePath + "ABOUT/github/")
  }

  multiGoldenTest("Can parse ABOUT-hosted dialectinstance", "ABOUT_hosted.%s") { config =>
    withDialect(
      "ABOUT-hosted-vcs-dialect.yaml",
      "ABOUT_hosted.yaml",
      config.golden,
      VocabularyYamlHint,
      target = Amf,
      directory = s"${basePath}ABOUT/",
      renderOptions = Some(config.renderOptions)
    )
  }

  // TODO migrate to multiGoldenTest
  test("Can parse and generate Instance dialect instance 1") {
    withDialect("dialect.yaml",
                "instance1.yaml",
                "instance1.json",
                VocabularyYamlHint,
                target = Amf,
                basePath + "Instagram/")
  }

  test("Can parse and generate Instance dialect instance 2") {
    withDialect("dialect.yaml",
                "instance2.yaml",
                "instance2.json",
                VocabularyYamlHint,
                target = Amf,
                basePath + "Instagram/")
  }

  test("Can parse activity instances") {
    withDialect("activity.yaml",
                "stream1.yaml",
                "stream1.json",
                VocabularyYamlHint,
                target = Amf,
                basePath + "streams/")
  }

  test("Can parse activity deployments demo") {
    withDialect("dialect.yaml",
                "deployment.yaml",
                "deployment.json",
                VocabularyYamlHint,
                target = Amf,
                basePath + "deployments_demo/")
  }
}

class DialectProductionResolutionTest extends FunSuiteCycleTests with DialectInstanceTester {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    AMLPlugin().resolve(unit, UnhandledErrorHandler)

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/production/"

  // Order is not predictable
  ignore("Can parse asyncapi overlay instances") {
    withDialect("dialect6.yaml",
                "patch6.yaml",
                "patch6.resolved.yaml",
                VocabularyYamlHint,
                Aml,
                basePath + "asyncapi/")
  }

}
