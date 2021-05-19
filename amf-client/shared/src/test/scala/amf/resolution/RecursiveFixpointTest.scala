package amf.resolution

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.FieldsFilter.All
import amf.core.model.domain.{AmfObject, RecursiveShape}
import amf.core.remote.Syntax.Yaml
import amf.core.remote.{AsyncApi20, Hint, Oas20YamlHint, Raml10YamlHint}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import org.mulesoft.common.collections.FilterType
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers.{contain, convertToAnyShouldWrapper}

import scala.concurrent.{ExecutionContext, Future}

class RecursiveFixpointTest() extends AsyncFunSuite with PlatformSecrets with ResolutionCapabilities {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/"

  case class RecursiveTestCase(path: String, hint: Hint)

  val testCases: Seq[RecursiveTestCase] = Seq(
    RecursiveTestCase("nested-library-with-recursive-shape/api.raml", Raml10YamlHint),
    RecursiveTestCase("additional-prop-recursive-shape/api.yaml", Oas20YamlHint),
    RecursiveTestCase("not-facet-recursive-shape/api.yaml", Hint(AsyncApi20, Yaml))
  )

  testCases.foreach { data =>
    test(s"Test fixpoint values in ${data.path}") {
      for {
        _ <- Validation(platform)
        unit <- AMFCompiler(s"file://$basePath${data.path}", platform, data.hint, eh = UnhandledErrorHandler)
          .build()
        _ <- Future(transform(unit, TransformationPipeline.EDITING_PIPELINE, data.hint.vendor))
      } yield {
        val elements                    = unit.iterator(fieldsFilter = All).toList
        val fixpointValues: Seq[String] = elements.filterType[RecursiveShape].map(_.fixpoint.value())
        val allIds                      = elements.collect { case o: AmfObject => o.id }.toSet
        allIds should contain allElementsOf (fixpointValues)
      }
    }
  }

}
