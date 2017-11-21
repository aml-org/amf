package amf.plugins.domain.graph

import amf.compiler.AbstractReferenceCollector
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model.YDocument

class AMFGraphReferenceCollector extends AbstractReferenceCollector{
  override def traverse(document: YDocument, validation: Validation, ctx: ParserContext) = Nil
}
