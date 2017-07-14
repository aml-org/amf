package amf.broker

import amf.common.AMFASTNode
import amf.domain.CreativeWork
import amf.remote.Vendor

/**
  *
  */
class CreativeWorkTreeMaker extends AMFTreeMaker[CreativeWork] {
  override def make(webApiSubClass: CreativeWork, vendor: Vendor): AMFASTNode = {
    makeStruct(("externalDocs", List(("url", webApiSubClass.url), ("description", webApiSubClass.description))))
  }
}

object CreativeWorkTreeMaker {
  def apply(webApiSubClass: CreativeWork): AMFASTNode = new CreativeWorkTreeMaker().make(webApiSubClass, null)
}
