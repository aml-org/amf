package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.model.domain.DataNode
import amf.core.parser.ErrorHandlingContext
import amf.core.utils.IdCounter
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.common.{DataNodeParser, DataNodeParserContext, ScalarNodeParser}
import org.yaml.model.YNode

object EnumParsing {
  val SCALAR_ENUM  = "SCALAR_ENUM"
  val UNKNOWN_ENUM = "UNKNOWN_ENUM"
}

object CommonEnumParser {

  def apply(parentId: String, enumType: String = EnumParsing.UNKNOWN_ENUM)(
      implicit ctx: ErrorHandlingContext with DataNodeParserContext): YNode => DataNode = {
    val enumParentId = s"$parentId/enum"
    enumType match {
      case EnumParsing.SCALAR_ENUM => ScalarNodeParser(parent = Some(enumParentId)).parse
      case _                       => DataNodeParser.parse(parent = Some(enumParentId), idCounter = new IdCounter())
    }
  }
}
