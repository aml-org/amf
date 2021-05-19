package amf.error

import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.parse.DefaultErrorHandler
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationResult
import amf.facades.Validation
import org.scalatest.{Assertion, AsyncFunSuite, Matchers, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

trait ParserErrorTest extends AsyncFunSuite with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val basePath: String

  protected def validate(file: String, fixture: AMFValidationResult => Unit*): Future[Assertion] = {
    validateWithUnit(file, (_: BaseUnit) => Unit, fixture)
  }

  protected def validateWithUnit(file: String,
                                 unitAssertion: BaseUnit => Unit,
                                 fixture: Seq[AMFValidationResult => Unit]): Future[Assertion] = {
    val client = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).createClient()
    Validation(platform).flatMap { _ =>
      client
        .parse(basePath + file)
        .map { amfResult =>
          unitAssertion(amfResult.bu)
          val report = amfResult.result.results
          if (report.size != fixture.size) {
            report.foreach(println)
            fail(s"Expected results has length of ${fixture.size} while actual results are ${report.size}")
          }
          fixture.zip(report).foreach {
            case (fn, result) => fn(result)
          }
          Succeeded
        }
    }
  }
}
