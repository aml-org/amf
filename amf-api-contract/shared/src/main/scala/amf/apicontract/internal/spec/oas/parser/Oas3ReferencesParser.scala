package amf.apicontract.internal.spec.oas.parser

import amf.apicontract.internal.spec.common.parser.{ReferencesParser, WebApiContext, WebApiRegister}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParsedReference, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.utils.AmfStrings
import org.yaml.model.YMap

object Oas3ReferencesParser {

  def apply(baseUnit: BaseUnit, root: Root)(implicit ctx: WebApiContext): ReferencesParser = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    new ReferencesParser(baseUnit, root.location, "uses".asOasExtension, map, root.references, new Oas3ApiRegister())
  }

  def apply(baseUnit: BaseUnit, rootLoc: String, key: String, map: YMap, references: Seq[ParsedReference])(implicit
      ctx: WebApiContext
  ): ReferencesParser = {
    new ReferencesParser(baseUnit, rootLoc, key, map, references, new Oas3ApiRegister())
  }
}
