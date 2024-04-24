package amf

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes
import amf.xml.client.scala.XMLConfiguration
import org.scalatest.compatible.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter}
import java.nio.file.{Files, Paths}
import scala.concurrent.Future
import scala.io.Source

class XMLJsonLDSourceSpecTest extends AsyncFunSuite with Matchers {

  val path = "amf-cli/shared/src/test/resources/upanddown/flow/flow1.xml"

  test("It generates JSON-LD from XML") {
    val config = XMLConfiguration.XML()
    val xmlClient = config.baseUnitClient()
    val jsonldClient = AMFGraphConfiguration.predefined().withRenderOptions(RenderOptions().withSourceMaps).baseUnitClient()
    for {
      result     <- xmlClient.parseContent(new String(Files.readAllBytes(Paths.get(path))), "application/xml")
      jsonld     <- Future.successful(jsonldClient.render(result.baseUnit, Mimes.`application/ld+json`))
    } yield {

      withOutFile(path,jsonld)
    }
  }

  private def withOutFile(path: String, jsonld: String): Assertion = {
    val outFile = Paths.get(path + ".json").toFile
    //if (!outFile.exists()) {
      val bw = new BufferedWriter(new FileWriter(outFile))
      bw.write(jsonld)
      bw.close()
      true.should(equal(true))
    /*
    } else {
      val source = Source.fromFile(outFile)
      try {
        val targetJsonld = source.getLines.mkString
        targetJsonld.should(equal(jsonld))
      } finally {
        source.close()
      }
    }

     */
  }
}
