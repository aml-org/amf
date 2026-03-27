package amf.shapes.test

import amf.core.client.scala.config.RenderOptions
import amf.core.io.FileAssertionTest
import amf.core.internal.remote.Mimes
import org.scalatest.Assertion

import scala.concurrent.Future

abstract class JsonSchemaBasedSpecCycleTestBase extends FileAssertionTest {

  def testConfig: JsonSchemaBasedSpecTestConfig

  private lazy val basePath      = testConfig.basePath
  private lazy val renderOptions = RenderOptions().withPrettyPrint
  private lazy val config        = testConfig.configuration.withRenderOptions(renderOptions)

  testConfig.cycleInstances.foreach { case CycleInstance(source, golden) =>
    test(s"Render ${testConfig.specName} instance $source to JSON-LD") {
      cycle(source, golden)
    }
  }

  def cycle(source: String, golden: String): Future[Assertion] = {
    for {
      parsed <- config.baseUnitClient().parse("file://" + basePath + source)
      actualString = config.baseUnitClient().render(parsed.baseUnit, Mimes.`application/ld+json`)
      actualFile <- writeTemporaryFile(golden)(actualString)
      assertion  <- assertDifferences(actualFile, basePath + golden)
    } yield {
      assertion
    }
  }
}
