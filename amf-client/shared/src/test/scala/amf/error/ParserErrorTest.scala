package amf.error

import amf.core.model.document.BaseUnit
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationResult
import amf.facades.Validation
import org.scalatest.{Assertion, AsyncFunSuite, Matchers, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

trait ParserErrorTest extends AsyncFunSuite with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val basePath: String

  protected def build(validation: Validation, file: String): Future[BaseUnit]

  protected def validate(file: String, fixture: (AMFValidationResult => Unit)*): Future[Assertion] = {
    Validation(platform).flatMap { validation =>
      build(validation, basePath + file)
        .map { _ =>
          val report = validation.aggregatedReport
          if (report.size != fixture.size) report.foreach(println)
          report.size should be(fixture.size)
          fixture.zip(report).foreach {
            case (fn, result) => fn(result)
          }
          Succeeded
        }
    }
  }
}
