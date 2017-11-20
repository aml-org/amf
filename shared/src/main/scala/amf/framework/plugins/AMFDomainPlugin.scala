package amf.framework.plugins

import amf.document.BaseUnit
import amf.spec.ParserContext
import org.yaml.model.YDocument

abstract class AMFDomainPlugin {

  def parse(document: YDocument, ctx: ParserContext): Option[BaseUnit]

}
