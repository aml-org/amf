package amf.apicontract.internal.plugins

import amf.apicontract.internal.spec.common.reference.ApiReferenceHandler
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.document.ReferenceHandler
import amf.core.internal.remote.{JSONRefs, JsonSchema, Spec}

trait ApiParsePlugin extends SpecAwareParsePlugin {

  override def priority: PluginPriority                                = NormalPriority
  override def allowRecursiveReferences: Boolean                       = true
  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new ApiReferenceHandler(id)
}

trait SpecAwareParsePlugin extends AMFParsePlugin with CrossSpecRestriction {
  override def validSpecsToReference: Seq[Spec] = Seq(JSONRefs, JsonSchema)
}
