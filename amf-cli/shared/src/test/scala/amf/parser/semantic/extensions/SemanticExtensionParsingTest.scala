package amf.parser.semantic.extensions

import amf.client.environment.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.model.document.BaseUnit
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

trait SemanticExtensionParsingTest extends AsyncFunSuite with FileAssertionTest {
  val basePath: String
  lazy val defaultConfiguration: AMFConfiguration = {
    WebAPIConfiguration
      .WebAPI()
      .merge(AsyncAPIConfiguration.Async20())

  }

  def cycle(source: String,
            target: String,
            dialect: String,
            subdirectory: String,
            targetMediaType: String,
            config: AMFConfiguration = defaultConfiguration): Future[Assertion] = {

    implicit val executionContext: ExecutionContext = defaultConfiguration.getExecutionContext

    for {
      config       <- config.withDialect(s"file://$basePath/$subdirectory/$dialect").map(_.asInstanceOf[AMFConfiguration])
      parsed       <- parse(source, target, subdirectory, config)
      resolved     <- Future.successful(transform(parsed, config))
      actualString <- Future.successful(render(resolved, config, targetMediaType))
      actualFile   <- writeTemporaryFile(target)(actualString)
      assertion    <- assertDifferences(actualFile, s"$basePath/$subdirectory/$target")
    } yield {
      assertion
    }

  }

  protected def parse(source: String,
                      target: String,
                      subdirectory: String,
                      config: AMFConfiguration): Future[BaseUnit] = {
    implicit val executionContext: ExecutionContext = defaultConfiguration.getExecutionContext
    config
      .withParsingOptions(config.options.parsingOptions.withBaseUnitUrl(s"file://$basePath/$subdirectory/$target"))
      .createClient()
      .parse(s"file://$basePath/$subdirectory/$source")
      .map(_.bu)
  }

  protected def transform(unit: BaseUnit, configuration: AMFConfiguration): BaseUnit = unit

  protected def render(unit: BaseUnit, config: AMFConfiguration, mediaType: String): String = {
    config.createClient().render(unit, mediaType)
  }

}
