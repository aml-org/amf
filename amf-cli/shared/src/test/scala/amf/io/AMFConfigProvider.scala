package amf.io

import amf.apicontract.client.scala.{AsyncAPIConfiguration, BaseApiConfiguration, OASConfiguration, RAMLConfiguration}
import amf.core.internal.remote.Vendor

trait AMFConfigProvider {

  protected def configFor(vendor: Vendor) = vendor match {
    case Vendor.RAML10  => RAMLConfiguration.RAML10()
    case Vendor.RAML08  => RAMLConfiguration.RAML08()
    case Vendor.OAS20   => OASConfiguration.OAS20()
    case Vendor.OAS30   => OASConfiguration.OAS30()
    case Vendor.ASYNC20 => AsyncAPIConfiguration.Async20()
    case Vendor.AMF     => BaseApiConfiguration.BASE()
  }
}
