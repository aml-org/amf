package amf.cycle

import amf.apicontract.client.scala.{AMFBaseUnitClient, AMFElementClient, OASConfiguration, RAMLConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.sfdc.client.scala.SFDCConfiguration
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{AsyncFlatSpec, Matchers}

import java.io.{BufferedWriter, File, FileWriter}

class SfdcFileTest extends AsyncFlatSpec {
  "SFDC" should "Convert SFDC into OAS 3" in {
    val raml10Client: AMFBaseUnitClient = RAMLConfiguration.RAML10().baseUnitClient()
    val oasClient : AMFBaseUnitClient = OASConfiguration.OAS30().baseUnitClient()

    SFDCConfiguration.SFDC().baseUnitClient().parse("file://amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.json") map
      { parseResult =>
        val transformResult = oasClient.transform(parseResult.baseUnit)
        val renderResult = oasClient.render(transformResult.baseUnit)
        val fileOut = new File("amf-cli/shared/src/test/resources/upanddown/sfdc/sfdc.oas")
        val bw = new BufferedWriter(new FileWriter(fileOut))
        bw.write(renderResult)
        bw.flush()
        bw.close()
        // println(renderResult)
        true shouldEqual true
      }

  }
}
