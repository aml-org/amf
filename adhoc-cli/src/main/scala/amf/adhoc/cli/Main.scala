package amf.adhoc.cli

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.remote.Mimes
import org.apache.commons.io.IOUtils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  case class JarResourceLoader() extends ResourceLoader {

    /** Fetch specified resource and return associated content. Resource should have been previously accepted.
      * If the resource doesn't exists, it returns a failed future caused by a ResourceNotFound exception. */
    override def fetch(resource: String): Future[Content] = {
      Future {
        val bytes   = IOUtils.toByteArray(getClass.getResourceAsStream(resource.stripPrefix("jar://")))
        val content = new String(bytes)
        new Content(content, resource)
      }
    }

    /** Checks if the resource loader accepts the specified resource. */
    override def accepts(resource: String): Boolean = resource.startsWith("jar://")
  }

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "parse" =>
        apiParse(args)

      case "validate" =>
        val configFuture = AMLConfiguration
          .predefined()
          .withResourceLoader(JarResourceLoader())
          .withDialect("jar:///dialects/validation-profile.yaml")
          .flatMap(_.withDialect("jar:///dialects/validation-report.yaml"))

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
    var parsedExtensions: Array[Dialect] = Array()
    val withSemex                        = args.contains("--extensions")
    if (withSemex) {
      val start = args.indexOf("--extensions") + 1
      val end = {
        val nextArgIdx = args.indexWhere(_.startsWith("--"), start)
        val lastIdx    = args.length
        math.max(nextArgIdx, lastIdx)
      }
      val extensions = args.slice(start, end)

      parsedExtensions = extensions
        .map { e =>
          Await.result(AMLConfiguration.predefined().baseUnitClient().parseDialect(s"file://$e"), Duration.Inf)
        }
        .map(_.dialect)
    }

    var parsingConf = APIConfiguration.API()
    parsedExtensions.foreach(e => parsingConf = parsingConf.withExtensions(e))

    val parsingFuture = parsingConf
      .baseUnitClient()
      .parse(s"file://${args(1)}")
    val parsing     = Await.result(parsingFuture, Duration.Inf)
    var baseUnit    = parsing.baseUnit
    val withLexical = args.contains("--with-lexical")

    var transformationConf = APIConfiguration.fromSpec(parsing.sourceSpec)
    parsedExtensions.foreach(e => transformationConf = transformationConf.withExtensions(e))

    val transformation =
      transformationConf
        .baseUnitClient()
        .transform(parsing.baseUnit, PipelineId.Editing)
    baseUnit = transformation.baseUnit

    val renderOptions = if (withLexical) {
      RenderOptions().withPrettyPrint.withSourceMaps.withSourceInformation
    } else {
      RenderOptions().withPrettyPrint
    }

    println {
      transformationConf
        .withRenderOptions(renderOptions)
        .baseUnitClient()
        .render(baseUnit, Mimes.`application/ld+json`)
    }
  }
}
