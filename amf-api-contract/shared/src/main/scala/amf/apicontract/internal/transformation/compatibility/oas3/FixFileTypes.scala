package amf.apicontract.internal.transformation.compatibility.oas3

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.{FileShape, ScalarShape}

/** To represent a method with file upload:
  *
  * in RAML: \- You define a body with multipart/form-data and a property of type: file \- You can specify acceptable
  * file types directly with fileTypes: ['application/xml']
  *
  * In OAS 3.0: \- You define a requestBody with content of type multipart/form-data \- it's schema should be of type:
  * object and have a property that uses type: string with format: binary
  */
case class FixFileTypes() extends TransformationStep() {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit =
    try {
      model
        .iterator()
        .foreach({
          case obj: AmfObject =>
            obj.fields.fields().toSeq.filter(fe => fe.value.value.isInstanceOf[FileShape]).foreach { fileField =>
              val field       = fileField.field
              val fileShape   = fileField.value.value.asInstanceOf[FileShape]
              val scalarShape = scalarFromShape(fileShape)
              obj.fields.removeField(field)
              obj.set(field, scalarShape)
            }
          case _ => // ignore
        })
      model
    } catch {
      case _: Throwable => model
    }

  private def scalarFromShape(s: Shape): ScalarShape = {
    val scalar = ScalarShape(s.annotations)
      .withFormat("binary")
      .withDataType(DataType.String)
    if (s.name.nonEmpty) scalar.withName(s.name.value())
    if (s.description.nonEmpty) scalar.withDescription(s.description.value())
    scalar
  }
}
