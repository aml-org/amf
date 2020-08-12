package amf.plugins.document.webapi.parser.spec.common

import amf.core.Root
import amf.core.parser.SyamlParsedDocument
import amf.core.remote.Syntax
import amf.core.remote.Syntax.Yaml
import amf.core.validation.SeverityLevels
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YNode, YType}

object YamlTagValidator {

  /**
    * validate that the node and its nested nodes do not contain any unknown yaml tags
    * values limited to json schema ruleset
    */
  def validate(root: Root)(implicit ctx: WebApiContext): Unit = {
    val severityLevel = getSeverityLevel
    Syntax unapply Some(root.mediatype) match {
      case Some(Yaml) | None =>
        val node = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        new YNodeIterator(node).foreach(validateTag(severityLevel))
      case _ => // tags can only be defined in yaml
    }
  }

  private def validateTag(severity: String)(node: YNode)(implicit ctx: WebApiContext): Unit = {
    val tagText = node.tag.text
    YType(tagText) match {
      case YType.Unknown | YType.Timestamp =>
        val ast = if (tagIsExplicit(node)) node.tag else node
        ctx.eh.reportConstraint(ParserSideValidations.UnknownYamlTag,
                                "",
                                s"Unknown tag '$tagText', must be allowed by json schema ruleset",
                                ast,
                                severity)
      case _ => // valid tag
    }
  }

  private def tagIsExplicit(node: YNode) = !node.tag.location.isZero

  private def getSeverityLevel(implicit ctx: WebApiContext): String = ctx match {
    case _: AsyncWebApiContext => SeverityLevels.VIOLATION
    case _                     => SeverityLevels.WARNING
  }
}
