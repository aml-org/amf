package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.domain.DataNode
import amf.core.client.scala.parse.document.ErrorHandlingContext
import amf.core.internal.datanode.{DataNodeParser, DataNodeParserContext, ScalarNodeParser}
import amf.core.internal.utils.IdCounter
import org.yaml.model.{IllegalTypeHandler, YNode}

object EnumParsing {
  val SCALAR_ENUM  = "SCALAR_ENUM"
  val UNKNOWN_ENUM = "UNKNOWN_ENUM"
}

object CommonEnumParser {

  def apply(parentId: String, enumType: String = EnumParsing.UNKNOWN_ENUM)(implicit
      ctx: ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler
  ): YNode => DataNode = {
    enumType match {
      case EnumParsing.SCALAR_ENUM => ScalarNodeParser().parse
      case _                       => DataNodeParser.parse(idCounter = new IdCounter())
    }
  }
}
