package amf.apiinstance.internal.spec.domain.flex

import amf.apiinstance.client.scala.model.domain.FilterRule
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import org.yaml.model.{YMap, YNode, YScalar, YSequence}

case class FlexRuleParser(policyMap: YMap)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {


  def parse(adopt: FilterRule => Unit): Unit = {
    traverse(policyMap).fetch("rules").arrayOr(()) { ruleNodes =>
      ruleNodes.nodes.foreach { ruleNode =>
        parseRule(ruleNode, adopt)
      }
    }

  }

  private def parseRule(ruleNode: YNode, adopt: FilterRule => Unit): Unit = {
    val rule = FilterRule(ruleNode)
    val ruleMap = traverse(ruleNode)
    ruleMap.fetch("path").string().foreach(path => rule.withPaths(List(path)))
    ruleMap.fetch("host").string().foreach(host => rule.withHosts(List(host)))
    ruleMap.fetch("methods").string().foreach(methods => rule.withHosts(List(methods)))
    ruleMap.fetch("headers").arrayOr(())(headers => {
      val headerValues = headers.nodes.map { header =>
        header.as[YScalar].text
      }
      rule.withHeaders(headerValues)
    })
    adopt(rule)
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
