package amf.shapes.internal.document.apicontract.validation.remote

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

protected[amf] class ErrorListener extends js.Object {
  def errorHook(path: js.Array[String], any: js.Any, `type`: js.Any): Unit = {
    throw new JsPayloadValidationError(
      s"""'$any' is not a valid value (of type '${`type`}') for '${path.mkString(".")}'"""
    )
  }
}

@js.native
protected[amf] trait AvroType extends js.Object {
  def isValid(payload: js.Any): Boolean                            = js.native
  def isValid(payload: js.Any, listener: ErrorListener): Boolean = js.native
}

@js.native
@JSImport("avro-js", JSImport.Default)
protected[amf] object AvroJs extends js.Object {
  def parse(schema: js.Any): AvroType = js.native
}

protected[amf] object AvroJsValidator {
  def apply(): AvroJs.type                     = AvroJs
  def parseJson(text: String): js.Any          = js.JSON.parse(text).asInstanceOf[js.Any]
  def parseJsonObject(text: String): js.Object = js.JSON.parse(text).asInstanceOf[js.Object]
}

protected[amf] object LazyAvroJs {
  lazy val default: AvroJs.type = AvroJsValidator.apply()
}
