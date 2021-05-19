package amf.extensions

import amf.client.parse.DefaultErrorHandler
import amf.core.client.ParsingOptions
import amf.core.emitter.RenderOptions
import amf.core.model.document.{BaseUnit, Document}
import amf.core.remote._
import amf.core.{AMF, AMFSerializer}
import amf.facades.AMFCompiler
import amf.io.FileAssertionTest
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml10Plugin}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class SecuritySchemeExtensionsTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  val basePath = "file://amf-client/shared/src/test/resources/extensions/security-schemes"

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

  private def renderToString(unit: BaseUnit, vendor: String): Future[String] =
    new AMFSerializer(unit, "application/json", vendor, RenderOptions()).renderToString

  private def parse(url: String, vendor: String, hint: Hint): Future[BaseUnit] = {
    AMFCompiler(url,
                platform,
                hint,
                eh = DefaultErrorHandler(),
                parsingOptions = ParsingOptions().withAmfJsonLdSerialization).build()
  }

  private def withBaseUnitPair(url: String, originalVendor: Vendor, otherVendor: Vendor)(
      assertion: List[BaseUnit] => Assertion) = {
    val fileName = url.split("/").last
    AMF.registerPlugin(Oas30Plugin)
    AMF.registerPlugin(Oas20Plugin)
    AMF.registerPlugin(Raml10Plugin)
    for {
      _             <- AMF.init()
      originalUnit  <- parse(url, originalVendor.name, hint(originalVendor))
      emittedApi    <- renderToString(originalUnit, otherVendor.name)
      tmp           <- writeTemporaryFile(fileName)(emittedApi)
      parsedApiUnit <- parse(s"file://${tmp.path}", otherVendor.name, hint(otherVendor))
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
