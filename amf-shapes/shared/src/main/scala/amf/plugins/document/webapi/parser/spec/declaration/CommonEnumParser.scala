package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.DataNode
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.parser.ShapeParserContext
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, DataNodeParserContext, ScalarNodeParser}
import org.yaml.model.YNode

object EnumParsing {
  val SCALAR_ENUM  = "SCALAR_ENUM"
  val UNKNOWN_ENUM = "UNKNOWN_ENUM"
}

object CommonEnumParser {

  def apply(parentId: String, enumType: String = EnumParsing.UNKNOWN_ENUM)(
      implicit ctx: DataNodeParserContext): YNode => DataNode = {
    val enumParentId = s"$parentId/enum"
    enumType match {
      case EnumParsing.SCALAR_ENUM =>
        ScalarNodeParser(parent = Some(enumParentId)).parse
      case _ =>
        node =>
          DataNodeParser.parse(parent = Some(enumParentId), idCounter = new IdCounter())(node)
    }
  }
}
