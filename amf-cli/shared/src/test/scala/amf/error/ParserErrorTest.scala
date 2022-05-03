package amf.error

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

trait ParserErrorTest extends AsyncFunSuite with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val basePath: String

  protected def validate(file: String, fixture: AMFValidationResult => Unit*): Future[Assertion] = {
    validateWithUnit(file, (_: BaseUnit) => Unit, fixture)
  }

  protected def validateWithUnit(
      file: String,
      unitAssertion: BaseUnit => Unit,
      fixture: Seq[AMFValidationResult => Unit]
  ): Future[Assertion] = {
    val client = APIConfiguration.API().baseUnitClient()
    client
      .parse(basePath + file)
      .map { amfResult =>
        unitAssertion(amfResult.baseUnit)
        val results = amfResult.results
        if (results.size != fixture.size) {
          results.foreach(println)
          fail(s"Expected results has length of ${fixture.size} while actual results are ${results.size}")
        }
        fixture.zip(results).foreach { case (fn, result) =>
          fn(result)
        }
        Succeeded
      }
  }
}
