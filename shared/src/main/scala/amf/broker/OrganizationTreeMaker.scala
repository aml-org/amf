package amf.broker

import amf.common.AMFASTNode
import amf.model.Organization
import amf.remote.Vendor

/**
  * Created by hernan.najles on 7/6/17.
  */
class OrganizationTreeMaker extends AMFTreeMaker[Organization] {

  override def make(organization: Organization, vendor: Vendor): AMFASTNode = {
    makeStruct(
      ("contact", List(("url", organization.url), ("name", organization.name), ("email", organization.email))))
  }

}

object OrganizationTreeMaker {
  def apply(organization: Organization): AMFASTNode = new OrganizationTreeMaker().make(organization, null)

}
