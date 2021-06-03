package amf.plugins.parse

import amf.client.remod.amfcore.plugins.{NormalPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.parse.AMFParsePlugin
import amf.core.errorhandling.AMFErrorHandler
import amf.core.parser.ReferenceHandler
import amf.core.remote.Vendor
import amf.plugins.document.webapi.CrossSpecRestriction
import amf.plugins.document.webapi.references.ApiReferenceHandler

trait ApiParsePlugin extends AMFParsePlugin with CrossSpecRestriction {

  protected def vendor: Vendor

  override val id: String                                              = vendor.name
  override def priority: PluginPriority                                = NormalPriority
  override def allowRecursiveReferences: Boolean                       = true
  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new ApiReferenceHandler(id)
  override def validMediaTypesToReference: Seq[String]                 = Seq("application/refs+json")
}
