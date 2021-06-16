package amf.shapes.internal.spec.common.emitter

import amf.shapes.internal.spec.common.TypeDef
import org.yaml.model.YType

object NumberTypeToYTypeConverter {

  def convert(datatype: TypeDef): YType = {
    datatype match {
      case TypeDef.IntType => YType.Int
      case _               => YType.Float
    }
  }

  def convert(datatype: Option[TypeDef]): YType = {
    this.convert(datatype.getOrElse(TypeDef.UndefinedType))
  }
}
