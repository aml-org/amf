package amf.agenticnetwork.client.platform

import amf.agenticnetwork.client.scala.{AgenticNetworkBaseUnitClient => InternalAgenticNetworkBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgenticNetworkBaseUnitClient private[amf] (private val _internal: InternalAgenticNetworkBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
