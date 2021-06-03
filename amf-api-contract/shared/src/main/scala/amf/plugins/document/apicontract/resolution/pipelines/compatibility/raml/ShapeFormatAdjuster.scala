package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.metamodel.ScalarShapeModel
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.shapes.models.TypeDef.{DateOnlyType, DateTimeOnlyType, DateTimeType, TimeOnlyType}
import amf.plugins.domain.shapes.parser.TypeDefXsdMapping

// TODO this is a bug, it's just adjusted to emit correctly for now.....
class ShapeFormatAdjuster() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case shape: ScalarShape if shape.format.nonEmpty =>
          val typeDef = TypeDefXsdMapping.typeDef(shape.dataType.value())
          if (typeDef != DateTimeOnlyType && typeDef != TimeOnlyType && typeDef != DateOnlyType) {
            val valid =
              if (typeDef == DateTimeType) Seq("rfc3339", "rfc2616")
              else Seq("int32", "int64", "int", "long", "float", "double", "int16", "int8")

            if (!valid.contains(shape.format.value())) shape.fields.removeField(ScalarShapeModel.Format)
          } else shape.fields.removeField(ScalarShapeModel.Format)
        case _ => // ignore
      }
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
