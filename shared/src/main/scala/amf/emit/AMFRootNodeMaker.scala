package amf.emit

import amf.common.AMFASTNode
import amf.common.AMFToken._
import amf.domain.APIDocumentation
import amf.remote.Vendor
import amf.spec.Spec

class AMFRootNodeMaker {

  def make(webApi: APIDocumentation, vendor: Vendor): AMFASTNode = {
    val amfast = Spec(vendor).emitter.emit(webApi.fields).build
    new AMFASTNode(Root, "", null, List(amfast))
  }

}

object AMFRootNodeMaker {
  def apply(webApi: APIDocumentation, vendor: Vendor): AMFASTNode = new AMFRootNodeMaker().make(webApi, vendor)

}
