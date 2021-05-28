package amf.maker

import amf.client.environment.RAMLConfiguration
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.compiler.CompilerTestBuilder
import amf.core.remote.{Raml08YamlHint, Raml10YamlHint}
import amf.core.validation.SeverityLevels
import amf.facades.Validation
import amf.{ProfileName, Raml08Profile, Raml10Profile}
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class DeprecatedKeysTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-client/shared/src/test/resources/maker/deprecatedwarnings/"

  case class FixtureResult(level: String, message: String)
  case class Fixture(name: String, file: String, profileName: ProfileName, results: Seq[FixtureResult])

  val fixture = List(
    Fixture(
      "deprecated schemas 10 warning",
      "schemas.raml",
      Raml10Profile,
      Seq(
        FixtureResult(SeverityLevels.WARNING,
                      "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead"))
    ),
    Fixture(
      "deprecated schema 10 warning",
      "schema.raml",
      Raml10Profile,
      Seq(
        FixtureResult(SeverityLevels.WARNING,
                      "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"))
    ),
    Fixture("schemas in 08 non warning", "schemas08.raml", Raml08Profile, Nil),
    Fixture("schema in 08 non warning", "schema08.raml", Raml08Profile, Nil)
  )

  fixture.foreach { f =>
    test("Test " + f.name) {
      val config = RAMLConfiguration.RAML()
      val client = config.createClient()
      for {
        validation  <- Validation(platform)
        parseResult <- client.parse(basePath + f.file)
        report      <- client.validate(parseResult.bu, f.profileName)
        unifiedReport <- Future.successful(
          if (!parseResult.conforms) parseResult.report
          else parseResult.report.merge(report)
        )
      } yield {
        assert(unifiedReport.conforms)
        assert(unifiedReport.results.lengthCompare(f.results.length) == 0)
        assert(
          !unifiedReport.results
            .zip(f.results)
            .map({
              case (result, fResult) =>
                assert(result.message.equals(fResult.message))
                assert(result.severityLevel.equals(fResult.level))
            })
            .exists(_ != succeed))
      }
    }

  }
}
