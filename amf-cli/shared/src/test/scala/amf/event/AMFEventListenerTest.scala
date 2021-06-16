package amf.event

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.platform.config.AMFEventNames
import amf.core.client.scala.config.{AMFEvent, AMFEventListener}
import amf.core.internal.parser.{AMFCompiler, ParseConfiguration}
import amf.core.internal.remote.{Cache, Context}
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.{Assertion, Matchers}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class AMFEventListenerTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  ignore("Test event listener in parsing") {
    val url = "file://amf-cli/shared/src/test/resources/validations/aip/ex1.raml"
    val expectedFrequency = Map(
      AMFEventNames.StartingParsing        -> 1,
      AMFEventNames.StartingContentParsing -> 2,
      AMFEventNames.ParsedSyntax           -> 2,
      AMFEventNames.ParsedModel            -> 2,
      AMFEventNames.FinishedParsing        -> 1
    )
    val listener = EventAccumulator()
    // TODO set listener in config.
    AMFCompiler(url,
                Some("application/yaml"),
                Context(platform),
                cache = Cache(),
                ParseConfiguration(RAMLConfiguration.RAML10())).build() map { _ =>
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
