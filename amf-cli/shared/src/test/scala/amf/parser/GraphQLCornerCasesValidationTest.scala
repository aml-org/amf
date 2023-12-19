package amf.parser

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Mimes.`application/ld+json`
import amf.graphql.client.scala.GraphQLConfiguration

import scala.concurrent.Future

class GraphQLCornerCasesValidationTest extends GraphQLValidationTest {
  override def basePath: String = "amf-cli/shared/src/test/resources/graphql/corner-cases"

  fs.syncFile(basePath)
    .list
    .groupBy(apiName)
    .values
    .collect {
      case toValidate if toValidate.length > 1 =>
        apiName(toValidate.head) // contains the API and it's report, thus should be validated
    }
    .foreach { api =>
      test(s"GraphQL corner cases > $api: report matches golden") {
        for {
          _         <- dumpJsonLDFor(api)
          assertion <- assertReport(s"$basePath/$api.graphql")
        } yield {
          assertion
        }
      }
    }

  private def apiName(api: String): String = api.split('.').dropRight(1).mkString(".")

  def dumpJsonLDFor(api: String): Future[Unit] = {
    val ro     = RenderOptions().withPrettyPrint.withCompactUris.withoutFlattenedJsonLd
    val client = GraphQLConfiguration.GraphQL().withRenderOptions(ro).baseUnitClient()
    for {
      parsing <- client.parse(s"file://$basePath/$api.graphql")
      content = client.render(parsing.baseUnit, `application/ld+json`)
      _ <- fs.asyncFile(s"$basePath/$api.jsonld").write(content)
    } yield {
      Unit
    }

  }
}
