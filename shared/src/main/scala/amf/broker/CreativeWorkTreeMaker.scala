package amf.broker

import amf.common.AMFASTNode
import amf.model.CreativeWork
import amf.remote.Vendor

/**
  * Created by hernan.najles on 7/6/17.
  */
class CreativeWorkTreeMaker extends AMFTreeMaker[CreativeWork] {
  override def make(webApiSubClass: CreativeWork, vendor: Vendor): AMFASTNode = {
    makeStruct(("externalDocs", List(("url", webApiSubClass.url), ("description", webApiSubClass.description))))
  }
}

object CreativeWorkTreeMaker {
  def apply(webApiSubClass: CreativeWork): AMFASTNode = new CreativeWorkTreeMaker().make(webApiSubClass, null)
}
