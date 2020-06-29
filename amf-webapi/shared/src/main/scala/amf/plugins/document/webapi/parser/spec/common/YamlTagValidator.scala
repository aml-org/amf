package amf.plugins.document.webapi.parser.spec.common

import amf.core.Root
import amf.core.parser.SyamlParsedDocument
import amf.core.remote.Syntax
import amf.core.remote.Syntax.Yaml
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YNode, YType}

object YamlTagValidator {

  /**
    * validate that the node and its nested nodes do not contain any unknown yaml tags
    * values limited to json schema ruleset
    */
  def validate(root: Root)(implicit ctx: WebApiContext): Unit = {
    Syntax unapply Some(root.mediatype) match {
      case Some(Yaml) | None =>
        val node = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        new YNodeIterator(node).foreach(validateTag)
      case _ => // tags can only be defined in yaml
    }

  }

  private def validateTag(node: YNode)(implicit ctx: WebApiContext): Unit = {
    val tagText = node.tag.text
    YType(tagText) match {
      case YType.Unknown | YType.Timestamp =>
        ctx.eh.violation(ParserSideValidations.UnknownYamlTag,
                         "",
                         s"Unknown tag '$tagText', must be allowed by json schema ruleset",
                         node.tag)
      case _ => // valid tag
    }
  }
}
