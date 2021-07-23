package amf.apicontract.internal.plugins

import amf.apicontract.internal.spec.common.reference.ApiReferenceHandler
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.ReferenceHandler
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.{Mimes, Vendor}

trait ApiParsePlugin extends AMFParsePlugin with CrossSpecRestriction {

  protected def vendor: Vendor

  override val id: String                                              = vendor.name
  override def priority: PluginPriority                                = NormalPriority
  override def allowRecursiveReferences: Boolean                       = true
  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new ApiReferenceHandler(id)
  override def validMediaTypesToReference: Seq[String]                 = Seq(`application/refs+json`)
}
