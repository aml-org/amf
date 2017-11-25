package amf.validation

import amf.core.validation.core.ValidationReport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("SHACLValidator")
class SHACLValidator extends amf.core.validation.core.SHACLValidator {

  var functionUrl: Option[String] = None
  var functionCode: Option[String] = None

  def nativeShacl: js.Dynamic = if (js.isUndefined(js.Dynamic.global.SHACLValidator)) {
    throw new Exception("Cannot find global SHACLValidator object")
  }  else {
    js.Dynamic.global.SHACLValidator
  }

  /**
    * Version of the validate function that retuern a JS promise instead of a Scala future
    * @param data string representation of the data graph
    * @param dataMediaType media type for the data graph
    * @param shapes string representation of the shapes graph
    * @param shapesMediaType media type fo the shapes graph
    * @return
    */
  @JSExport("validate")
  def validateJS(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): js.Promise[String] =
    validate(data, dataMediaType, shapes, shapesMediaType).toJSPromise

  override def report(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): Future[ValidationReport] = {
    val promise = Promise[ValidationReport]()
    val validator = js.Dynamic.newInstance(nativeShacl)()
    loadLibrary(validator)
    validator.validate(data, dataMediaType, shapes, shapesMediaType, { (e: js.Error, report: js.Dynamic) =>
      if (js.isUndefined(e) || e == null) {
        val result = new JSValidationReport(report)
        promise.success(result)
      } else {
        promise.failure(js.JavaScriptException(e))
      }
    })
    promise.future
  }

  /**
    * Version of the report function that retuern a JS promise instead of a Scala future
    * @param data string representation of the data graph
    * @param dataMediaType media type for the data graph
    * @param shapes string representation of the shapes graph
    * @param shapesMediaType media type fo the shapes graph
    * @return
    */
  @JSExport("report")
  def reportJS(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): js.Promise[ValidationReport] =
    report(data, dataMediaType, shapes, shapesMediaType).toJSPromise

  /**
    * Registers a library in the validator
    *
    * @param url
    * @param code
    * @return
    */
  override def registerLibrary(url: String, code: String): Unit = {
    this.functionUrl = Some(url)
    this.functionCode = Some(code)
  }

  override def validate(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): Future[String] = {
    val promise = Promise[String]()
    val validator = js.Dynamic.newInstance(nativeShacl)()
    loadLibrary(validator)
    validator.validate(data, dataMediaType, shapes, shapesMediaType, { (e: js.Dynamic, r: js.Dynamic) =>
      if (js.isUndefined(e) || e == null) {
        promise.success(js.Dynamic.global.JSON.stringify(r).toString)
      } else {
        promise.failure(js.JavaScriptException(e))
      }
    })
    promise.future
  }

  protected def loadLibrary(validator: js.Dynamic): Unit = {
    if (functionCode.isDefined && functionUrl.isDefined) {
      validator.registerJSCode(functionUrl.get, functionCode.get)
    }
  }

}
