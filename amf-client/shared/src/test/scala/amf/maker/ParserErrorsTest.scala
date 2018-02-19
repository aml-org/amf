package amf.maker

import amf.ProfileNames
import amf.compiler.CompilerTestBuilder
import amf.core.remote.RamlYamlHint
import amf.core.validation.SeverityLevels
import amf.facades.Validation
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class ParserErrorsTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-client/shared/src/test/resources/maker/parsererrors/"

  case class FixtureResult(level: String, message: String)
  case class Fixture(name: String, file: String, profileName: String, results: Seq[FixtureResult])

  val fixture = List(
    Fixture(
      "overflow number",
      "overflow-number.raml",
      ProfileNames.RAML,
      Seq(FixtureResult(SeverityLevels.VIOLATION, "Cannot parse '9223372036854776000' with tag '?'"))
    )
  )

  fixture.foreach { f =>
    test("Test " + f.name) {
      for {
        validation <- Validation(platform)
        model      <- build(basePath + f.file, RamlYamlHint, validation = Option(validation))
        report     <- validation.validate(model, f.profileName)
      } yield {
        assert(!report.conforms)
        assert(report.results.lengthCompare(f.results.length) == 0)
        assert(
          !report.results
            .zip(f.results)
            .map({
              case (result, fResult) =>
                assert(result.message.contains(fResult.message))
                assert(result.level.equals(fResult.level))
            })
            .exists(_ != succeed))
      }
    }

  }
}
