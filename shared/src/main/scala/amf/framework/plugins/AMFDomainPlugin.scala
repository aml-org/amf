package amf.framework.plugins

import amf.compiler.Root
import amf.document.BaseUnit
import amf.spec.ParserContext

abstract class AMFDomainPlugin {

  def parse(document: Root, ctx: ParserContext): Option[BaseUnit]

}
