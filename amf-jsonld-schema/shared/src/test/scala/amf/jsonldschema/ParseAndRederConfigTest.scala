package amf.jsonldschema

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.core.client.scala.AMFGraphConfiguration
import amf.jsonldschema.client.scala.JsonLDSchemaConfiguration
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class ParseAndRenderConfigTest extends AsyncFunSuite with FileAssertionTest {
  val basePath: String                   = "file://amf-jsonld-schema/shared/src/test/resources/"
  override implicit val executionContext = ExecutionContext.global
  test("Array configuration") {
    val predefined = JsonLDSchemaConfiguration.predefined().baseUnitClient()
    for {
      config <- platform.fetchContent(basePath + "configs/array.json", AMFGraphConfiguration.predefined())(
        executionContext)
      converter     <- predefined.parseDialect(basePath + "policies/array.json")
      dialectS      <- Future(predefined.render(converter.dialect, "application/yaml"))
      cicledDialect <- AMLConfiguration.predefined().baseUnitClient().parseContent(dialectS)
      instance      <- predefined.parsePayloadWithDialect(config.toString, cicledDialect.baseUnit.asInstanceOf[Dialect])
      render        <- Future { predefined.emitPayloadWithDialect(instance, cicledDialect.baseUnit.asInstanceOf[Dialect]) }
      d             <- writeTemporaryFile(s"$basePath/golden/array.json")(render)
      r             <- assertDifferences(d, s"$basePath/golden/array.json")
    } yield r
  }
}
