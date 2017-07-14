package amf.broker

import amf.common.AMFASTNode
import amf.domain.License
import amf.remote.Vendor

/**
  *
  */
class LicenseTreeMaker extends AMFTreeMaker[License] {

  override def make(webApiSubClass: License, vendor: Vendor): AMFASTNode = {
    makeStruct(("license", List(("url", webApiSubClass.url), ("name", webApiSubClass.name))))
  }
}

object LicenseTreeMaker {
  def apply(webApiSubClass: License): AMFASTNode = new LicenseTreeMaker().make(webApiSubClass, null)
}
