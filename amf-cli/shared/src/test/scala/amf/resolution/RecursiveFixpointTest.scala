package amf.resolution

import amf.core.client.common.transform._
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.FieldsFilter.All
import amf.core.client.scala.model.domain.{AmfObject, RecursiveShape}
import amf.core.internal.remote.Syntax.Yaml
import amf.core.internal.remote.{AsyncApi20, Hint, Oas20YamlHint, Raml10YamlHint}
import amf.core.internal.unsafe.PlatformSecrets
import amf.testing.ConfigProvider
import org.mulesoft.common.collections.FilterType
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class RecursiveFixpointTest() extends AsyncFunSuite with Matchers with PlatformSecrets with ResolutionCapabilities {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/resolution/"

  case class RecursiveTestCase(path: String, hint: Hint)

  val testCases: Seq[RecursiveTestCase] = Seq(
    RecursiveTestCase("nested-library-with-recursive-shape/api.raml", Raml10YamlHint),
    RecursiveTestCase("additional-prop-recursive-shape/api.yaml", Oas20YamlHint),
    RecursiveTestCase("not-facet-recursive-shape/api.yaml", Hint(AsyncApi20, Yaml))
  )

  testCases.foreach { data =>
    test(s"Test fixpoint values in ${data.path}") {
      val config = ConfigProvider.configFor(data.hint.spec)
      for {
        parseResult <- config
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .baseUnitClient()
          .parse(s"file://$basePath${data.path}")
        _ <- Future(transform(parseResult.baseUnit, PipelineId.Editing, data.hint.spec, config))
      } yield {
        val elements                    = parseResult.baseUnit.iterator(fieldsFilter = All).toList
        val fixpointValues: Seq[String] = elements.filterType[RecursiveShape].map(_.fixpoint.value())
        val allIds                      = elements.collect { case o: AmfObject => o.id }.toSet
        allIds should contain allElementsOf (fixpointValues)
      }
    }
  }

}
