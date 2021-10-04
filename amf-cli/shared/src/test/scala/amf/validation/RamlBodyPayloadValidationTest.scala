package amf.validation

import amf.apicontract.client.scala.{AMFBaseUnitClient, WebAPIConfiguration}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.{StrictValidationMode, ValidationMode}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes.`application/json`
import amf.core.internal.remote._
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.MediaTypeMatcher
import amf.testing.ConfigProvider
import amf.testing.ConfigProvider.configFor
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
              Option(`application/json`)),
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

  override protected val basePath: String = "file://amf-cli/shared/src/test/resources/validations/body-payload/"

  override def transform(unit: BaseUnit, client: AMFBaseUnitClient): BaseUnit = {

    unit.sourceSpec match {
      case Some(Raml08) =>
        client.transform(unit, PipelineId.Default).baseUnit
      case _ =>
        client.transform(unit, PipelineId.Default).baseUnit
    }
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

  def transform(unit: BaseUnit, config: AMFBaseUnitClient): BaseUnit

  protected def fixtureList: Seq[Fixture]

  protected def validate(api: String,
                         payload: String,
                         mediaType: Option[String],
                         givenHint: Hint): Future[AMFValidationReport] = {
    val config = configFor(givenHint.spec)
    val client = config.baseUnitClient()
    for {
      model <- client
        .parse(api)
        .map(_.baseUnit)
        .map(transform(_, client))
      result <- {
        val shape = findShape(model.asInstanceOf[Document])
        mediaType
          .map(mediaTypeVal => {
            config
              .elementClient()
              .payloadValidatorFor(shape, mediaTypeVal, validationMode)
              .validate(payload)
          })
          .getOrElse(
            config
              .elementClient()
              .payloadValidatorFor(shape, payload.guessMediaType(false), validationMode)
              .validate(payload))
      }
    } yield {
      result
    }
  }

  fixtureList.foreach { f =>
    test("Test " + f.name) {
      validate(basePath + f.api, f.payload, f.mediaType, f.hint).map(_.conforms should be(f.conforms))
    }
  }

  def validationMode: ValidationMode = StrictValidationMode
}
