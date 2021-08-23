package amf.cycle

import amf.aml.client.scala.AMLDialectResult
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{AmfJsonHint, AmlHint}
import amf.core.internal.unsafe.PlatformSecrets
import amf.emit.AMFRenderer
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaDialectCycleTest extends AsyncFunSuite with PlatformSecrets with FileAssertionTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "amf-cli/shared/src/test/resources/cycle/jsonschemadialects/"

  test("can generate a dialect from a semantic JSON schema") {
    cycle("simple.json", "simple.yaml")
  }

  test("can generate a dialect from a semantic JSON schema with allOfs") {
    cycle("allOf.json", "allOf.yaml")
  }

  test("can generate a dialect from a semantic JSON schema with oneOfs") {
    cycle("oneOf.json", "oneOf.yaml")
  }

  def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined()
    config.baseUnitClient().parseDialect(path)
  }

  private def cycle(path: String, golden: String): Future[Assertion] = {
    val finalPath   = "file://" + basePath + path
    val finalGolden = "file://" + basePath + golden
    for {

      dialect <- parseSchema(finalPath).map(_.dialect)
      r <- {
        val expected = emit(dialect)
        writeTemporaryFile(finalGolden)(expected).flatMap(s => assertDifferences(s, finalGolden))
      }
    } yield r

  }

  private def emit(unit: BaseUnit)(implicit executionContext: ExecutionContext): String = {
    val options =
      RenderOptions().withCompactUris.withoutSourceMaps.withoutRawSourceMaps.withFlattenedJsonLd.withPrettyPrint
    new AMFRenderer(unit, AmlHint, options).renderToString
  }
}
