package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.shapes.models.TypeDef.{DateOnlyType, DateTimeOnlyType, DateTimeType, TimeOnlyType}
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping

// TODO this is a bug, it's just adjusted to emit correctly for now.....
class ShapeFormatAdjuster()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model.findByType(ScalarShapeModel.`type`.head.iri()).foreach {
        case shape: ScalarShape if shape.format.nonEmpty =>
          val typeDef = TypeDefXsdMapping.typeDef(shape.dataType.value())
          if (typeDef != DateTimeType && typeDef != DateTimeOnlyType && typeDef != TimeOnlyType && typeDef != DateOnlyType) {
            val valid = Seq("int32", "int64", "int", "long", "float", "double", "int16", "int8")

            if (!valid.contains(shape.format.value())) shape.fields.removeField(ScalarShapeModel.Format)
          }
        case _ => // ignore
      }
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
