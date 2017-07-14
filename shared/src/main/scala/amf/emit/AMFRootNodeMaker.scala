package amf.emit

import amf.common.AMFASTNode
import amf.common.AMFToken._
import amf.model.BaseWebApi
import amf.remote.Vendor
import amf.spec.Spec

class AMFRootNodeMaker {

  def make(webApi: BaseWebApi, vendor: Vendor): AMFASTNode = {
    val amfast = Spec(vendor).emitter.emit(webApi.fields).build()
    new AMFASTNode(Root, "", null, List(amfast))
  }

}

object AMFRootNodeMaker {
  def apply(webApi: BaseWebApi, vendor: Vendor): AMFASTNode = new AMFRootNodeMaker().make(webApi, vendor)

}
