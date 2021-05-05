package amf.plugins.document.webapi.parser.spec.declaration.emitters.emitter

import amf.core.metamodel.Field
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.spec.declaration.JSONSchemaDraft201909SchemaVersion
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.validations.ShapePayloadValidations.UntranslatableDraft2019Fields

case class UntranslatableDraft2019FieldsPresentGuard[T <: Shape](shape: T, fields: Seq[Field], fieldNames: Seq[String])(
    implicit spec: ShapeEmitterContext) {

  def evaluateOrRun(toRun: () => Unit): Unit = {
    val fieldsSeq                      = shape.fields.fields().map(_.field).toSeq
    val fieldsContainsNonValidateField = fields.exists(fieldsSeq.contains)
    if (!isDraft2019OrBigger && fieldsContainsNonValidateField && spec.options.shouldEmitWarningForUnsupportedValidationFacets)
      spec.eh.warning(UntranslatableDraft2019Fields,
                      shape.id,
                      s"${fieldNamesString(fieldNames)} won't be used for validation",
                      shape.annotations)
    else
      toRun()
  }

  private def fieldNamesString(fieldNames: Seq[String]) =
    if (fieldNames.size == 1) fieldNames.head
    else fieldNames.foldLeft("")((acc, curr) => s"$acc, $curr").drop(1)
  private def isDraft2019OrBigger = spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)
}
