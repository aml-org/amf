package amf.validation

import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.Shape
import amf.core.remote._
import amf.core.services.PayloadValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.{RAML08Plugin, RAML10Plugin}
import amf.plugins.domain.webapi.models.WebApi
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Future

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
        conforms = true
      ))

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

  override protected val hint: Hint       = OasJsonHint
  override protected val basePath: String = "file://amf-client/shared/src/test/resources/validations/body-payload/"

  override def transform(unit: BaseUnit): BaseUnit =
    unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi].sourceVendor match {
      case Some(Raml08) => RAML08Plugin.resolve(unit)
      case _            => RAML10Plugin.resolve(unit)
    }
}

trait ApiShapePayloadValidationTest extends AsyncFunSuite with Matchers with PlatformSecrets {

  protected val basePath: String

  // todo: transform fixture to have more than one payload by api, so we don't need to parse multiple times same api.
  protected case class Fixture(name: String, api: String, payload: String, conforms: Boolean)

  protected val hint: Hint

  protected def findShape(d: Document): Shape

  def transform(unit: BaseUnit): BaseUnit

  protected def fixtureList: Seq[Fixture]

  protected def validate(api: String, payload: String): Future[AMFValidationReport] =
    for {
      validation <- Validation(platform)
      model <- AMFCompiler(api, platform, hint, validation)
        .build()
        .map(transform)
      result <- {
        val shape = findShape(model.asInstanceOf[Document])
        PayloadValidator.validate(shape, payload, SeverityLevels.VIOLATION)
      }
    } yield {
      result
    }

  fixtureList.foreach { f =>
    test("Test " + f.name) {
      validate(basePath + f.api, f.payload).map(_.conforms should be(f.conforms))
    }
  }
}
