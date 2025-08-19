package amf.brokergroup.client.platform

import amf.brokergroup.client.scala.{BrokerGroupBaseUnitClient => InternalBrokerGroupBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BrokerGroupBaseUnitClient private[amf] (private val _internal: InternalBrokerGroupBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
