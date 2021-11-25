package amf.adhoc.cli

import amf.aml.client.scala.AMLConfiguration
import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "parse" =>
        apiParse(args)

      case "validate" =>
        val configFuture = AMLConfiguration
          .predefined()
          .withDialect(
            "https://raw.githubusercontent.com/aml-org/models/master/src/main/dialects/validation-profile.yaml")
          .flatMap(_.withDialect(
            "https://raw.githubusercontent.com/aml-org/models/master/src/main/dialects/validation-report.yaml"))

        val config = Await.result(configFuture, Duration.Inf)

        val client = config.baseUnitClient()

        val parsing    = Await.result(client.parseDialectInstance(s"file://${args(1)}"), Duration.Inf)
        val validation = Await.result(client.validate(parsing.baseUnit), Duration.Inf)

        val merged = parsing.merge(validation)
        println(merged.toString)
        if (merged.conforms) {
          System.exit(0)
        } else {
          System.exit(1)
        }
      case c =>
        System.err.println(s"Unrecognized command $c. Accepted commands are 'parse' and 'validate'")
        System.exit(1)
    }
  }

  private def apiParse(args: Array[String]): Unit = {
    // For comprehensions skip Exceptions like File Not Found Exception
    val parsingFuture = APIConfiguration
      .API()
      .baseUnitClient()
      .parse(s"file://${args(1)}")
    val parsing     = Await.result(parsingFuture, Duration.Inf)
    var baseUnit    = parsing.baseUnit
    val withLexical = args.length > 2 && args(2) == "--with-lexical"

    val transformation =
      APIConfiguration
        .fromSpec(parsing.sourceSpec)
        .baseUnitClient()
        .transform(parsing.baseUnit, PipelineId.Editing)
    baseUnit = transformation.baseUnit

    val renderOptions = if (withLexical) {
      RenderOptions().withPrettyPrint.withSourceMaps.withSourceInformation
    } else {
      RenderOptions().withPrettyPrint
    }

    println {
      APIConfiguration
        .API()
        .withRenderOptions(renderOptions)
        .baseUnitClient()
        .render(baseUnit, Mimes.`application/ld+json`)
    }
  }
}
