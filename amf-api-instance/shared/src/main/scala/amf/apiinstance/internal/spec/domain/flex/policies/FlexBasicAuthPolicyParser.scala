package amf.apiinstance.internal.spec.domain.flex.policies

import amf.apiinstance.client.scala.model.domain.PolicyDomainElement
import amf.apiinstance.client.scala.model.domain.policies.BasicAuthPolicy
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import org.yaml.model.YMap

case class FlexBasicAuthPolicyParser(policy: YMap)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {

  def parse(adopt: PolicyDomainElement => Unit): Unit = {
    val basicAuth = BasicAuthPolicy(policy)
    val node = traverse(policy).errorFor(basicAuth)

    node.fetch("username").string() match {
      case Some(username) => basicAuth.withUsername(username)
      case _              => // ignore
    }
    node.fetch("password").string() match {
      case Some(password) => basicAuth.withPassword(password)
      case _              => // ignore
    }

    adopt(basicAuth)
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
