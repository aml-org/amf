package amf.event

import amf.core.client.scala.config.event.{AMFEventReportBuilder, TimedEvent}
import amf.core.client.scala.config.{AMFEvent, GroupedEvent}
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.io.FileAssertionTest
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class AMFEventReportBuilderTest
    extends AsyncBeforeAndAfterEach
    with PlatformSecrets
    with Matchers
    with FileAssertionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Report creation") {
    val report = AMFEventReportBuilder(events).build()
    report.startTime should equal(1)
    report.endTime should equal(27)
    report.totalTime should equal(26)
    val logs = report.logs
    logs.find(l => l.eventName == "group-a-2").get.groupedDelta should equal(6)
    logs.find(l => l.eventName == "group-b-3").get.groupedDelta should equal(4)
    logs.find(l => l.eventName == "group-d-1-lonely").get.groupedDelta should equal(0)
  }

  test("Report printing") {
    val pathToReport = "amf-cli/shared/src/test/resources/event/event-report.golden.txt"
    val report       = AMFEventReportBuilder(events).build()
    writeTemporaryFile(pathToReport)(report.toString()).flatMap { file =>
      assertDifferences(file, pathToReport)
    }
  }

  private def events = Seq(
    TimedEvent(1, MyMockGroupedEvent("group-a-0", "group-a")),
    TimedEvent(1, MyMockLonelyEvent("group-c-1-lonely")),
    TimedEvent(3, MyMockGroupedEvent("group-a-1", "group-a")),
    TimedEvent(6, MyMockLonelyEvent("group-a-lonely")),
    TimedEvent(7, MyMockGroupedEvent("group-b-0", "group-b")),
    TimedEvent(9, MyMockGroupedEvent("group-a-2", "group-a")),
    TimedEvent(13, MyMockLonelyEvent("group-e-lonely")),
    TimedEvent(15, MyMockGroupedEvent("group-a-3", "group-a")),
    TimedEvent(19, MyMockGroupedEvent("group-b-1", "group-b")),
    TimedEvent(20, MyMockGroupedEvent("group-b-2", "group-b")),
    TimedEvent(23, MyMockLonelyEvent("group-d-1-lonely")),
    TimedEvent(24, MyMockGroupedEvent("group-b-3", "group-b")),
    TimedEvent(26, MyMockLonelyEvent("group-c-lonely")),
    TimedEvent(27, MyMockGroupedEvent("group-b-4", "group-b"))
  )

  case class MyMockGroupedEvent(name: String, groupKey: String) extends AMFEvent with GroupedEvent
  case class MyMockLonelyEvent(name: String)                    extends AMFEvent
}
