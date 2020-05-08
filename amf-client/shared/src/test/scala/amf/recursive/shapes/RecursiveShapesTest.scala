package amf.recursive.shapes

import amf.core.model.document.BaseUnit
import amf.core.remote.{Hint, Oas20, RamlYamlHint}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.validation.MultiPlatformReportGenTest
import amf.{ProfileName, Raml10Profile}
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class RecursiveShapesTest extends FunSuiteCycleTests with MultiPlatformReportGenTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/recursives/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/recursives/"
  override val hint: Hint          = RamlYamlHint

  override protected lazy val defaultProfile: ProfileName = Raml10Profile

  private case class RecursiveShapeFixture(api: String, report: String, oasGoldenPath: String)
  private val fixture: Seq[RecursiveShapeFixture] =
    Seq(
      RecursiveShapeFixture("props1.raml", "props1.report", "props1.json"),
      RecursiveShapeFixture("props2.raml", "props2.report", "props2.json"),
      RecursiveShapeFixture("props2rev.raml", "props2rev.report", "props2rev.json"),
      RecursiveShapeFixture("props3.raml", "props3.report", "props3.json"),
      RecursiveShapeFixture("items1.raml", "items1.report", "items1.json"),
      RecursiveShapeFixture("union1.raml", "union1.report", "union1.json"),
      RecursiveShapeFixture("inherits-and-props.raml", "inherits-and-props.report", "inherits-and-props.json"),
      RecursiveShapeFixture("response-without-mediatype.raml", "response-without-mediatype.report", "response-without-mediatype.json"),
    )

  fixture.foreach { rf =>
    test("Test validation " + rf.api) {
      validate(rf.api, Some(rf.report))
    }

    test("Test emission " + rf.api) {
      cycle(rf.api, rf.oasGoldenPath)
    }

  }

  def cycle(source: String, golden: String): Future[Assertion] = {
      super.cycle(source,
                  "oas/" + golden,
                  RamlYamlHint,
                  Oas20,
                  directory = basePath.replace("file://", ""))
  }

  /** Method for transforming parsed unit. Override if necessary. */
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = ValidationResolutionPipeline(Raml10Profile, unit)
}
