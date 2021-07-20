package amf.extensions

import amf.apicontract.client.scala.{AsyncAPIConfiguration, OASConfiguration, RAMLConfiguration, WebAPIConfiguration}
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.remote._
import amf.core.internal.render.AMFSerializer
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class RamlToOasSecuritySchemeExtensionsTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  val basePath = "file://amf-cli/shared/src/test/resources/extensions/security-schemes"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Raml oauth 1.0 scheme should be emitted as OAS extension") {
    withBaseUnitPair(s"$basePath/oauth1.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  test("Raml oauth 2.0 scheme should be emitted as OAS security scheme in OAS 2") {
    withBaseUnitPair(s"$basePath/oauth2.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  test("Raml basic auth should be emitted as OAS security scheme in OAS 2") {
    withBaseUnitPair(s"$basePath/basicAuth.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  test("Raml digest auth should be emitted as OAS extension") {
    withBaseUnitPair(s"$basePath/digestAuth.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  test("Raml pass through should be emitted as OAS extension") {
    withBaseUnitPair(s"$basePath/passThrough.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  test("Raml security extension should be emitted as OAS extension") {
    withBaseUnitPair(s"$basePath/customSecurity.raml", Vendor.RAML10, Vendor.OAS20) { x =>
      {
        x match {
          case List(ramlUnit, oasUnit) =>
            val oasSchemeName = oasUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            val ramlSchemeName =
              ramlUnit.asInstanceOf[Document].declares.head.asInstanceOf[SecurityScheme].name.value()
            ramlSchemeName shouldBe oasSchemeName
        }
      }
    }
  }

  private def renderToString(unit: BaseUnit, mediaType: String): String = {
    val config = OASConfiguration.OAS20()
    new AMFSerializer(unit, mediaType, config.renderConfiguration).renderToString
  }

  private def parse(url: String, vendor: String, hint: Hint, amfConfig: AMFGraphConfiguration): Future[BaseUnit] =
    amfConfig.baseUnitClient().parse(url).map(_.baseUnit)

  private def withBaseUnitPair(url: String, originalVendor: Vendor, otherVendor: Vendor)(
      assertion: List[BaseUnit] => Assertion) = {
    val fileName = url.split("/").last
    val config   = WebAPIConfiguration.WebAPI().withParsingOptions(ParsingOptions().withAmfJsonLdSerialization)
    for {
      originalUnit  <- parse(url, originalVendor.mediaType, hint(originalVendor), config)
      emittedApi    <- Future.successful(renderToString(originalUnit, otherVendor.mediaType))
      tmp           <- writeTemporaryFile(fileName)(emittedApi)
      parsedApiUnit <- parse(s"file://${tmp.path}", otherVendor.name, hint(otherVendor), config)
    } yield {
      assertion(List(originalUnit, parsedApiUnit))
    }
  }

  private def hint(vendor: Vendor): Hint = vendor match {
    case Vendor.RAML10 => Raml10YamlHint
    case Vendor.RAML08 => Raml08YamlHint
    case Vendor.OAS20  => Oas20YamlHint
    case Vendor.OAS30  => Oas30YamlHint
    case _             => throw new IllegalArgumentException("Vendor is not recognized")
  }
}
