package amf.extensions

import amf.apicontract.client.scala.{AsyncAPIConfiguration, OASConfiguration, RAMLConfiguration, WebAPIConfiguration}
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.remote._
import amf.core.internal.render.AMFSerializer
import amf.io.FileAssertionTest
import amf.testing.ConfigProvider
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class RamlToOasSecuritySchemeExtensionsTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  val basePath = "file://amf-cli/shared/src/test/resources/extensions/security-schemes"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Raml oauth 1.0 scheme should be emitted as OAS extension") {
    withBaseUnitPair(s"$basePath/oauth1.raml", Oas20JsonHint) { x =>
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
    withBaseUnitPair(s"$basePath/oauth2.raml", Oas20JsonHint) { x =>
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
    withBaseUnitPair(s"$basePath/basicAuth.raml", Oas20JsonHint) { x =>
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
    withBaseUnitPair(s"$basePath/digestAuth.raml", Oas20JsonHint) { x =>
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
    withBaseUnitPair(s"$basePath/passThrough.raml", Oas20JsonHint) { x =>
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
    withBaseUnitPair(s"$basePath/customSecurity.raml", Oas20JsonHint) { x =>
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

  private def renderToString(unit: BaseUnit, target: Hint): String = {
    val config = ConfigProvider.configFor(target.spec)
    new AMFSerializer(unit, config.renderConfiguration, Some(target.syntax.mediaType)).renderToString
  }

  private def parse(url: String, amfConfig: AMFGraphConfiguration): Future[BaseUnit] =
    amfConfig.baseUnitClient().parse(url).map(_.baseUnit)

  private def withBaseUnitPair(url: String, target: Hint)(assertion: List[BaseUnit] => Assertion) = {
    val fileName = url.split("/").last
    val config   = WebAPIConfiguration.WebAPI().withParsingOptions(ParsingOptions().withAmfJsonLdSerialization)
    for {
      originalUnit  <- parse(url, config)
      emittedApi    <- Future.successful(renderToString(originalUnit, target))
      tmp           <- writeTemporaryFile(fileName)(emittedApi)
      parsedApiUnit <- parse(s"file://${tmp.path}", config)
    } yield {
      assertion(List(originalUnit, parsedApiUnit))
    }
  }

  private def hint(spec: Spec): Hint = spec match {
    case Spec.RAML10 => Raml10YamlHint
    case Spec.RAML08 => Raml08YamlHint
    case Spec.OAS20  => Oas20YamlHint
    case Spec.OAS30  => Oas30YamlHint
    case _           => throw new IllegalArgumentException("Vendor is not recognized")
  }
}
