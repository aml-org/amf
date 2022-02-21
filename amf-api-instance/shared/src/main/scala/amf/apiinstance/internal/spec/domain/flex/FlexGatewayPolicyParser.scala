package amf.apiinstance.internal.spec.domain.flex

import amf.apiinstance.client.scala.model.domain.PolicyDomainElement
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.spec.domain.flex.policies.FlexBasicAuthPolicyParser
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import org.yaml.model.YMap

case class FlexGatewayPolicyParser(policyMap: YMap)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {
  override def error_handler: AMFErrorHandler = ctx.eh


  def parse(adopt: PolicyDomainElement => Unit): Unit = {
    traverse(policyMap).fetch("policyRef").fetch("name").string() foreach { policyName =>
      parseDefaultPolicy(policyName, adopt)
    }

  }

  def parseDefaultPolicy(policyName: String, adopt: PolicyDomainElement => Unit): Unit = {
    traverse(policyMap).fetch("config").map {
      case Some(configNode) =>
        policyName match {
          case "basic-authentication" => FlexBasicAuthPolicyParser(configNode).parse(adopt)
        }
      case _                => // TODO: record violation
    }

  }
}
