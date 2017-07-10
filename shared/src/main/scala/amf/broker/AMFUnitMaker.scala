package amf.broker

import amf.model.BaseWebApi
import amf.parser.{AMFUnit, Document}
import amf.remote.Vendor

/**
  * Created by hernan.najles on 7/7/17.
  */
class AMFUnitMaker {

  def make(webApi: BaseWebApi, vendor: Vendor): AMFUnit = {
    AMFUnit(AMFRootNodeMaker(webApi, vendor), webApi.host, Document, vendor)
  }
}

object AMFUnitMaker {
  def apply(webApi: BaseWebApi, vendor: Vendor): AMFUnit = new AMFUnitMaker().make(webApi, vendor)
}
