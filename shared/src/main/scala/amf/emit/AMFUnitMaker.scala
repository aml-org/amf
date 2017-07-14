package amf.emit

import amf.domain.APIDocumentation
import amf.parser.{AMFUnit, Document}
import amf.remote.Vendor

/**
  *
  */
class AMFUnitMaker {

  def make(webApi: APIDocumentation, vendor: Vendor): AMFUnit = {
    AMFUnit(AMFRootNodeMaker(webApi, vendor), webApi.host, Document, vendor)
  }
}

object AMFUnitMaker {
  def apply(webApi: APIDocumentation, vendor: Vendor): AMFUnit = new AMFUnitMaker().make(webApi, vendor)
}
