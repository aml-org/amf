package amf.shapes.client.jsonldschema.spec

import amf.core.client.scala.config.RenderOptions
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.ExecutionContext

class JsonLDSchemaSpecParsingTest extends AsyncFunSuite with FileAssertionTest {
  private lazy val basePath: String      = "amf-shapes/shared/src/test/resources/jsonld-schema/"
  private lazy val schemasPath: String   = basePath + "schemas/"
  private lazy val instancesPath: String = basePath + "instances/"
  private lazy val resultsPath: String   = basePath + "instances/results/"

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(RenderOptions().withPrettyPrint).baseUnitClient()

  /** First I iterate all directories recursively starting from base path, filtering those files that have assigned an
    * instance path at instances equivalent directory.
    */
  def goldens = computeFolder("")

  private def computeFolder(path: String): List[String] = {
    platform.fs
      .syncFile(schemasPath + path)
      .list
      .flatMap { schema =>
        val newPath = path + "/" + schema
        if (platform.fs.syncFile(schemasPath + newPath).isDirectory) computeFolder(newPath)
        else if (platform.fs.syncFile(instancesPath + newPath).exists) Some(newPath)
        else None
      }
      .toList
  }

  goldens.foreach { path =>
    if (path.endsWith(".ignore")) ignore(s"Test case $path") { run(path) }
    else
      test(s"Test case $path") { run(path) }
  }

  def run(schema: String) = {
    for {
      jsonDocument <- client.parseJsonLDSchema("file://" + schemasPath + schema).map(_.jsonDocument)
      instance     <- client.parseJsonLDInstance("file://" + instancesPath + schema, jsonDocument).map(_.instance)
      tmp <- writeTemporaryFile("file://" + resultsPath + schema + ".jsonld")(
        client.render(instance, "application/schemald+json")
      )
      r <- assertDifferences(tmp, "file://" + resultsPath + schema + ".jsonld")
    } yield r

  }

}
