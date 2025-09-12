package amf.othercard.client.platform

import amf.othercard.client.scala.{OtherCardBaseUnitClient => InternalOtherCardBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class OtherCardBaseUnitClient private[amf] (private val _internal: InternalOtherCardBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
