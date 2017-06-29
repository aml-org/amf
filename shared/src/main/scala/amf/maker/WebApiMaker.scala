package amf.maker

import amf.builder.WebApiBuilder
import amf.model.WebApi
import amf.parser.AMFUnit

/**
  * Created by martin.gutierrez on 6/29/17.
  */
class WebApiMaker(unit: AMFUnit) extends Maker[WebApi](unit.vendor) {

  override def make: WebApi = {
    val builder = WebApiBuilder()
    val spec    = VendorSpec.fromVendor(unit.vendor)

    builder
      .withName(findValue(unit, "title"))
      .withDescription(findValue(unit, "description"))
      .withHost(findValue(unit, "baseUri"))
      .withProtocols(findValues(unit, "protocols"))

    builder.build
  }
}
