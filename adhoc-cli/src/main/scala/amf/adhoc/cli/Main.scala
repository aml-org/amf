package amf.adhoc.cli

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFResult
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.resource.{ClasspathResourceLoader, ResourceLoader}
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes
import amf.core.internal.validation.core.ValidationReport
import amf.graphql.client.scala.GraphQLConfiguration
import org.apache.commons.io.IOUtils
import amf.shapes.client.scala.config.JsonLDSchemaConfiguration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main {

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "parse" =>
        apiParse(args)

      case "parse-with-schema" =>
        parseWithSchema(args)

      case "validate-api" =>
        val path = s"file://${args(1)}"
        val fut = for {
          parsing <- APIConfiguration.API().baseUnitClient().parse(path)
          transform <- Future.successful {
            APIConfiguration.fromSpec(parsing.sourceSpec).baseUnitClient().transform(parsing.baseUnit, PipelineId.Cache)
          }
          validation <- APIConfiguration.fromSpec(parsing.sourceSpec).baseUnitClient().validate(transform.baseUnit)
        } yield {
          val all = parsing.results ++ transform.results ++ validation.results
          AMFValidationReport(validation.model, validation.profile, results = all)
        }
        val report = Await.result(fut, Duration.Inf)
        println(report.toString)
      case "validate-graphql" =>
        val path = s"file://${args(1)}"
        val client = GraphQLConfiguration.GraphQL().baseUnitClient()
        val fut = for {
          parsing <- client.parse(path)
          transform <- Future.successful { client.transform(parsing.baseUnit, PipelineId.Cache) }
          validation <- client.validate(transform.baseUnit)
        } yield {
          val all = parsing.results ++ transform.results ++ validation.results
          AMFValidationReport(validation.model, validation.profile, results = all)
        }
        val report = Await.result(fut, Duration.Inf)
        println(report.toString)
      case "validate" =>
        val merged: AMFResult = validateInstance(args(1))
        println(merged.toString)
        if (merged.conforms) {
          System.exit(0)
        } else {
          System.exit(1)
        }
      case c =>
        System.err.println(
          s"Unrecognized command $c. Accepted commands are 'parse' and 'validate' (rulesets) and 'validate-api' (apis)"
        )
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
    val pipeline = if (args.contains("--pipeline")) {
      val pipelineArgIdx = args.indexOf("--pipeline") + 1
      args(pipelineArgIdx)
    } else {
      PipelineId.Editing
    }

    var transformationConf = APIConfiguration.fromSpec(parsing.sourceSpec)
    parsedExtensions.foreach(e => transformationConf = transformationConf.withExtensions(e))

    val transformation =
      transformationConf
        .baseUnitClient()
        .transform(parsing.baseUnit, pipeline)
    baseUnit = transformation.baseUnit

    val renderOptions = if (withLexical) {
      RenderOptions().withPrettyPrint.withSourceMaps.withSourceInformation
    } else {
      RenderOptions().withPrettyPrint.withGovernanceMode
    }

    println {
      transformationConf
        .withRenderOptions(renderOptions)
        .baseUnitClient()
        .render(baseUnit, Mimes.`application/ld+json`)
    }
  }

  private def parseWithSchema(args: Array[String]): Unit = {
    val renderOptions = RenderOptions().withPrettyPrint
    val client        = JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(renderOptions).baseUnitClient()
    val res = for {
      schemaResult   <- client.parseJsonLDSchema(s"file://${args(1)}")
      instanceResult <- client.parseJsonLDInstance(s"file://${args(2)}", schemaResult.jsonDocument)
    } yield {
      val rendered = client.render(instanceResult.baseUnit, Mimes.`application/ld+json`)
      println(rendered)
    }
    Await.result(res, Duration.Inf)
  }

  def validateInstance(path: String): AMFResult = {
    val configFuture = for {
      jarConfig <- Future.successful(
        AMLConfiguration.predefined().withResourceLoaders(List(AdaptedClassPathResourceLoader()))
      )
      profileDialect <- jarConfig.baseUnitClient().parseDialect("file:///dialects/validation-profile.yaml")
      reportDialect  <- jarConfig.baseUnitClient().parseDialect("file:///dialects/validation-report.yaml")
    } yield {
      AMLConfiguration.predefined().withDialect(profileDialect.dialect).withDialect(reportDialect.dialect)
    }

    val config = Await.result(configFuture, Duration.Inf)

    val client = config.baseUnitClient()

    val parsing    = Await.result(client.parseDialectInstance(s"file://${path}"), Duration.Inf)
    val validation = Await.result(client.validate(parsing.dialectInstance), Duration.Inf)

    parsing.merge(validation)
  }
}

case class AdaptedClassPathResourceLoader() extends ResourceLoader {

  override def fetch(resource: String): Future[Content] = {
    val strippedPrefix  = resource.stripPrefix("file://")
    val eventualContent = ClasspathResourceLoader.fetch(strippedPrefix)
    eventualContent.map { c =>
      c.copy(url = resource)
    }
  }

  override def accepts(resource: String): Boolean = true
}
