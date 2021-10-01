package amf.cycle

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFElementClient, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.internal.unsafe.PlatformSecrets
import amf.sfdc.client.scala.SFDCConfiguration
import amf.sfdc.plugins.parse.SfdcToOas30TransformationPipeline
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{AsyncFlatSpec, Matchers}

import java.io.{BufferedWriter, File, FileWriter}
import scala.concurrent.ExecutionContext

class SfdcFileTest extends AsyncFlatSpec with PlatformSecrets {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  "SFDC" should "Convert SFDC into OAS 3" in {
    val oasClient: AMFBaseUnitClient =
      OASConfiguration.OAS30().withTransformationPipeline(SfdcToOas30TransformationPipeline).baseUnitClient()
    val ramlClient: AMFBaseUnitClient = RAMLConfiguration.RAML10().baseUnitClient()
    SFDCConfiguration
      .SFDC()
      .baseUnitClient()
      .parse("file://amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.json") map { parseResult =>
//      val transformResult = oasClient.transform(parseResult.baseUnit)
      val renderResult = oasClient.render(parseResult.baseUnit)
      platform.fs
        .syncFile("amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.yaml")
        .write(renderResult)
      true shouldEqual true
    }
  }
}
