package amf.maker

import amf.apicontract.client.scala.config.RAMLConfiguration
import amf.compiler.CompilerTestBuilder
import amf.core.client.common.validation.{ProfileName, Raml08Profile, Raml10Profile, SeverityLevels}
import amf.core.client.scala.validation.AMFValidationReport
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class DeprecatedKeysTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-cli/shared/src/test/resources/maker/deprecatedwarnings/"

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
        parseResult <- client.parse(basePath + f.file)
        report      <- client.validate(parseResult.bu, f.profileName)
        unifiedReport <- {
          val parseReport = AMFValidationReport.unknownProfile(parseResult)
          Future.successful(
            if (!parseResult.conforms) parseReport
            else parseReport.merge(report)
          )
        }
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
