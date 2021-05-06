package amf.event

import amf.client.remod.amfcore.config.{AMFEventListener, AMFEvent, AMFEventNames}
import amf.core.remote.{Cache, Context, Raml10}
import amf.core.services.RuntimeCompiler
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.{Assertion, Matchers}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class AMFEventListenerTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override protected def beforeEach(): Future[Unit] = {
    Validation.apply(platform).map(_.init())
  }

  ignore("Test event listener in parsing") {
    val url = "file://amf-client/shared/src/test/resources/validations/aip/ex1.raml"
    val expectedFrequency = Map(
      AMFEventNames.STARTING_PARSING         -> 1,
      AMFEventNames.STARTING_CONTENT_PARSING -> 2,
      AMFEventNames.PARSED_SYNTAX            -> 2,
      AMFEventNames.PARSED_MODEL             -> 2,
      AMFEventNames.FINISHED_PARSING         -> 1
    )
    val listener = EventAccumulator()
    // TODO set listener in config.
    RuntimeCompiler(url, Some("application/yaml"), Some(Raml10.name), Context(platform), cache = Cache()) map { _ =>
      assertEventFrequencies(expectedFrequency, listener)
    }
  }

  private def assertEventFrequencies(expected: Map[String, Int], accum: EventAccumulator): Assertion = {
    val actualFrequencies: Map[String, Int] = accum.names.groupBy(identity).mapValues(_.size)
    expected.toSet should equal(actualFrequencies.toSet)
  }

  private case class EventAccumulator(names: ListBuffer[String] = new ListBuffer[String]) extends AMFEventListener {
    override def notifyEvent(event: AMFEvent): Unit = names.append(event.name)
  }

}
