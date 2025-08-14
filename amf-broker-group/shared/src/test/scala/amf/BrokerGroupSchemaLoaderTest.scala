package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.brokergroup.internal.plugins.parse.schema.BrokerGroupSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class BrokerGroupSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that BrokerGroup Schema has no errors") {
    BrokerGroupSchemaLoader.doc != null shouldBe true
    BrokerGroupSchemaLoader.schema != null shouldBe true
    BrokerGroupSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    BrokerGroupSchemaLoader.errors.size shouldBe 0
  }
}
