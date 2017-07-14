package amf.broker

import amf.common.AMFASTNode
import amf.domain.Organization
import amf.remote.Vendor

/**
  *
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
