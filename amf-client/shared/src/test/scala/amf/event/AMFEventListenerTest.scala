package amf.event

import amf.client.environment.RAMLConfiguration
import amf.client.remod.ParseConfiguration
import amf.client.exported.config.AMFEventNames
import amf.client.remod.amfcore.config.{AMFEvent, AMFEventListener}
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
      AMFEventNames.StartingParsing        -> 1,
      AMFEventNames.StartingContentParsing -> 2,
      AMFEventNames.ParsedSyntax           -> 2,
      AMFEventNames.ParsedModel            -> 2,
      AMFEventNames.FinishedParsing        -> 1
    )
    val listener = EventAccumulator()
    // TODO set listener in config.
    RuntimeCompiler(Some("application/yaml"),
                    Context(platform),
                    cache = Cache(),
                    new ParseConfiguration(RAMLConfiguration.RAML10(), url)) map { _ =>
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
