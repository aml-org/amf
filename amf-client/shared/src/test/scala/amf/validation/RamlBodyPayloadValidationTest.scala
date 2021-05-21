package amf.validation

import amf.client.parse.DefaultParserErrorHandler
import amf.client.plugins.{StrictValidationMode, ValidationMode}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Shape
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.RuntimeResolver
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.{Raml08Plugin, Raml10Plugin}
import amf.plugins.domain.shapes.validation.PayloadValidationPluginsHandler
import amf.plugins.domain.webapi.models.api.WebApi
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class RamlBodyPayloadValidationTest extends ApiShapePayloadValidationTest {

  protected def fixtureList =
    Seq(
      Fixture(
        "Properties and patter properties schema",
        "props-pattern-props/json-ref.raml",
        """{
                                    |  "/": {
                                    |    "fstype": "ext4",
                                    |    "device": "/dev/hda"
                                    |  },
                                    |  "swap": {
                                    |    "fstype": "ext4",
                                    |    "device": "/dev/hdb"
                                    |  }
                                    |}""".stripMargin,
        conforms = true,
        hint = Raml08YamlHint
      ),
      Fixture("Required pattern property", "required-pattern-prop.raml", "element-2: 2", conforms = true),
      Fixture("Big number payload",
              "big-number-payload.raml",
              "{\"in\": 22337203685477999090}",
              conforms = true,
              Option("application/json")),
      Fixture("Invalid required pattern property",
              "required-pattern-prop.raml",
              "invalid-element-2: 2",
              conforms = false)
    )

  override protected def findShape(d: Document): Shape =
    d.encodes
      .asInstanceOf[WebApi]
      .endPoints
      .head
      .operations
      .head
      .request
      .payloads
      .head
      .schema

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/validations/body-payload/"

  override def transform(unit: BaseUnit): BaseUnit =
    unit.asInstanceOf[Document].sourceVendor match {
      case Some(Raml08) =>
        RuntimeResolver.resolve(Raml08.name, unit, TransformationPipeline.DEFAULT_PIPELINE, unit.errorHandler())
      case _ =>
        RuntimeResolver.resolve(Raml10.name, unit, TransformationPipeline.DEFAULT_PIPELINE, unit.errorHandler())
    }
}

trait ApiShapePayloadValidationTest extends AsyncFunSuite with Matchers with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val basePath: String

  // todo: transform fixture to have more than one payload by api, so we don't need to parse multiple times same api.
  protected case class Fixture(name: String,
                               api: String,
                               payload: String,
                               conforms: Boolean,
                               mediaType: Option[String] = None,
                               hint: Hint = Raml10YamlHint)

  protected def findShape(d: Document): Shape

  def transform(unit: BaseUnit): BaseUnit

  protected def fixtureList: Seq[Fixture]

  protected def validate(api: String,
                         payload: String,
                         mediaType: Option[String],
                         givenHint: Hint): Future[AMFValidationReport] =
    for {
      _ <- Validation(platform)
      model <- AMFCompiler(api, platform, givenHint, eh = DefaultParserErrorHandler.withRun())
        .build()
        .map(transform)
      result <- {
        val shape = findShape(model.asInstanceOf[Document])
        mediaType
          .map(mediaTypeVal => {
            PayloadValidationPluginsHandler
              .validate(shape, mediaTypeVal, payload, SeverityLevels.VIOLATION, validationMode = validationMode)
          })
          .getOrElse(PayloadValidationPluginsHandler
            .validateWithGuessing(shape, payload, SeverityLevels.VIOLATION, validationMode = validationMode))
      }
    } yield {
      result
    }

  fixtureList.foreach { f =>
    test("Test " + f.name) {
      validate(basePath + f.api, f.payload, f.mediaType, f.hint).map(_.conforms should be(f.conforms))
    }
  }

  def validationMode: ValidationMode = StrictValidationMode
}
