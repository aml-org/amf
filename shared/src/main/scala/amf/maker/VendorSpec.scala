package amf.maker

import amf.remote.{Oas, Raml, Vendor}

/**
  * Created by martin.gutierrez on 6/29/17.
  */
case class VendorSpec(vendor: Vendor)

class RamlSpec extends VendorSpec(Raml) {}

class OasSpec extends VendorSpec(Oas) {}

object VendorSpec {
  def fromVendor(vendor: Vendor): VendorSpec = vendor match {
    case Raml => new RamlSpec
    case Oas  => new OasSpec
  }
}
