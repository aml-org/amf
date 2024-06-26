package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.shapes.internal.spec.common.JSONSchemaDraft201909SchemaVersion
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.UntranslatableDraft2019Fields

case class UntranslatableDraft2019FieldsPresentGuard[T <: Shape](shape: T, fields: Seq[Field], fieldNames: Seq[String])(
    implicit spec: ShapeEmitterContext
) {

  def evaluateOrRun(toRun: () => Unit): Unit = {
    val fieldsSeq                      = shape.fields.fields().map(_.field).toSeq
    val fieldsContainsNonValidateField = fields.exists(fieldsSeq.contains)
    if (
      !isDraft2019OrBigger && fieldsContainsNonValidateField && spec.options.shouldEmitWarningForUnsupportedValidationFacets
    )
      spec.eh.warning(
        UntranslatableDraft2019Fields,
        shape.id,
        s"${fieldNamesString(fieldNames)} won't be used for validation",
        shape.annotations
      )
    else
      toRun()
  }

  private def fieldNamesString(fieldNames: Seq[String]) =
    if (fieldNames.size == 1) fieldNames.head
    else fieldNames.foldLeft("")((acc, curr) => s"$acc, $curr").drop(1)
  private def isDraft2019OrBigger = spec.schemaVersion.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)
}
