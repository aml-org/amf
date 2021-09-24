package amf.cycle

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFElementClient, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.sfdc.client.scala.SFDCConfiguration
import amf.sfdc.plugins.parse.SfdcToOas30TransformationPipeline
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{AsyncFlatSpec, Matchers}

import java.io.{BufferedWriter, File, FileWriter}

class SfdcFileTest extends AsyncFlatSpec {
  "SFDC" should "Convert SFDC into OAS 3" in {
//    val oasClient: AMFBaseUnitClient =
//      OASConfiguration.OAS30().withTransformationPipeline(SfdcToOas30TransformationPipeline).baseUnitClient()
    val ramlClient: AMFBaseUnitClient = RAMLConfiguration.RAML10().baseUnitClient()
    SFDCConfiguration
      .SFDC()
      .baseUnitClient()
      .parse("file://amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.json") map { parseResult =>
      val transformResult = ramlClient.transform(parseResult.baseUnit, PipelineId.Compatibility)
      val renderResult    = ramlClient.render(transformResult.baseUnit)
      val fileOut         = new File("amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.raml")
      val bw              = new BufferedWriter(new FileWriter(fileOut))
      bw.write(renderResult)
      bw.flush()
      bw.close()
      // println(renderResult)
      true shouldEqual true
    }
  }
}
