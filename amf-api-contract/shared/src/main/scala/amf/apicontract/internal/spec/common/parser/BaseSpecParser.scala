package amf.apicontract.internal.spec.common.parser

import amf.aml.internal.parse.common.DeclarationKeyCollector
import amf.apicontract.internal.validation.definitions.ParserSideValidations.PathTemplateUnbalancedParameters
import amf.core.internal.parser.domain.BaseSpecParser
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import org.yaml.model._

trait WebApiBaseSpecParser extends BaseSpecParser with SpecParserOps with DeclarationKeyCollector

trait SpecParserOps extends QuickFieldParserOps {

  protected def checkBalancedParams(path: String,
                                    value: YNode,
                                    node: String,
                                    property: String,
                                    ctx: WebApiContext): Unit = {
    val pattern1 = "\\{[^}]*\\{".r
    val pattern2 = "}[^{]*}".r
    if (pattern1.findFirstMatchIn(path).nonEmpty || pattern2.findFirstMatchIn(path).nonEmpty) {
      ctx.eh.violation(
        PathTemplateUnbalancedParameters,
        node,
        Some(property),
        "Invalid path template syntax",
        value
      )
    }
  }
}
