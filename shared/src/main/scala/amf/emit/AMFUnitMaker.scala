package amf.emit

import amf.domain.WebApi
import amf.parser.{AMFUnit, Document}
import amf.remote.Vendor

/**
  *
  */
class AMFUnitMaker {

  def make(webApi: WebApi, vendor: Vendor): AMFUnit = {
    AMFUnit(AMFRootNodeMaker(webApi, vendor), webApi.host, Document, vendor)
  }
}

object AMFUnitMaker {
  def apply(webApi: WebApi, vendor: Vendor): AMFUnit = new AMFUnitMaker().make(webApi, vendor)
}
