package amf.plugins.features.validation

import amf.AmfProfile
import amf.client.execution.BaseExecutionEnvironment
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.rdf.{RdfModel, RdfModelEmitter}
import amf.core.services.ValidationOptions
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.{ValidationReport, ValidationSpecification}
import amf.plugins.features.validation.emitters.ValidationRdfModelEmitter

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("SHACLValidator")
class SHACLValidator extends amf.core.validation.core.SHACLValidator with PlatformSecrets {

  var functionUrl: Option[String]  = None
  var functionCode: Option[String] = None

  def nativeShacl: js.Dynamic =
    if (js.isUndefined(js.Dynamic.global.GlobalSHACLValidator)) {
      throw new Exception("Cannot find global SHACLValidator object")
    } else {
      js.Dynamic.global.GlobalSHACLValidator
    }

  /**
    * Version of the validate function that return a JS promise instead of a Scala future
    * @param data string representation of the data graph
    * @param dataMediaType media type for the data graph
    * @param shapes string representation of the shapes graph
    * @param shapesMediaType media type fo the shapes graph
    * @return
    */
  @JSExport("validate")
  def validateJS(data: String,
                 dataMediaType: String,
                 shapes: String,
                 shapesMediaType: String,
                 exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): js.Promise[String] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    validate(data, dataMediaType, shapes, shapesMediaType).toJSPromise
  }

  override def report(data: String, dataMediaType: String, shapes: String, shapesMediaType: String)(
      implicit executionContext: ExecutionContext): Future[ValidationReport] = {
    val promise = Promise[ValidationReport]()
    try {
      val validator = js.Dynamic.newInstance(nativeShacl)()
      loadLibrary(validator)

      val dataModel   = platform.rdfFramework.get.syntaxToRdfModel(dataMediaType, data).get
      val shapesModel = platform.rdfFramework.get.syntaxToRdfModel(shapesMediaType, shapes).get

      validator.validateFromModels(
        dataModel.model.native().asInstanceOf[js.Dynamic],
        shapesModel.model.native().asInstanceOf[js.Dynamic], { (e: js.Dynamic, report: js.Dynamic) =>
          if (js.isUndefined(e) || e == null) {
            val repeater: js.Array[js.Any] = js.Array()
            val result                     = new JSValidationReport(report)
            promise.success(result)
          } else {
            promise.failure(js.JavaScriptException(e))
          }
        }
      )

      promise.future
    } catch {
      case e: Exception =>
        promise.failure(e).future
    }
  }

  /**
    * Version of the report function that returns a JS promise instead of a Scala future
    * @param data string representation of the data graph
    * @param dataMediaType media type for the data graph
    * @param shapes string representation of the shapes graph
    * @param shapesMediaType media type fo the shapes graph
    * @return
    */
  @JSExport("report")
  def reportJS(data: String,
               dataMediaType: String,
               shapes: String,
               shapesMediaType: String,
               exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): js.Promise[ValidationReport] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    report(data, dataMediaType, shapes, shapesMediaType).toJSPromise
  }

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

  override def validate(data: String, dataMediaType: String, shapes: String, shapesMediaType: String)(
      implicit executionContext: ExecutionContext): Future[String] = {
    val promise   = Promise[String]()
    val validator = js.Dynamic.newInstance(nativeShacl)()
    loadLibrary(validator)
    validator.validate(
      data,
      dataMediaType,
      shapes,
      shapesMediaType, { (e: js.Dynamic, r: js.Dynamic) =>
        if (js.isUndefined(e) || e == null) {
          promise.success(js.Dynamic.global.JSON.stringify(r).toString)
        } else {
          promise.failure(js.JavaScriptException(e))
        }
      }
    )
    promise.future
  }

  protected def loadLibrary(validator: js.Dynamic): Unit = {
    if (functionCode.isDefined && functionUrl.isDefined) {
      validator.registerJSCode(functionUrl.get, functionCode.get)
    }
  }

  override def validate(data: BaseUnit, shapes: Seq[ValidationSpecification], options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[String] = {
    val promise = Promise[String]()
    try {
      ExecutionLog.log("SHACLValidator#validate: Creating SHACL-JS instance and loading JS libraries")
      val validator = js.Dynamic.newInstance(nativeShacl)()
      loadLibrary(validator)

      ExecutionLog.log("SHACLValidator#validate: loading Jena data model")
      val dataModel = new RdflibRdfModel()
      new RdfModelEmitter(dataModel).emit(data, options.toRenderOptions)
      ExecutionLog.log("SHACLValidator#validate: loading Jena shapes model")
      val shapesModel = new RdflibRdfModel()
      new ValidationRdfModelEmitter(options.messageStyle.profileName, shapesModel).emit(shapes)
      validator.validateFroModels(
        dataModel.model,
        shapesModel.model, { (e: js.Dynamic, r: js.Dynamic) =>
          if (js.isUndefined(e) || e == null) {
            promise.success(js.Dynamic.global.JSON.stringify(r).toString)
          } else {
            promise.failure(js.JavaScriptException(e))
          }
        }
      )

      promise.future
    } catch {
      case e: Exception =>
        promise.failure(e).future
    }
  }

  override def report(data: BaseUnit, shapes: Seq[ValidationSpecification], options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[ValidationReport] = {
    val promise = Promise[ValidationReport]()
    try {
      ExecutionLog.log("SHACLValidator#validate: Creating SHACL-JS instance and loading JS libraries")
      val validator = js.Dynamic.newInstance(nativeShacl)()
      loadLibrary(validator)

      ExecutionLog.log("SHACLValidator#validate: loading Jena data model")
      val dataModel = new RdflibRdfModel()
      new RdfModelEmitter(dataModel).emit(data, RenderOptions().withValidation)
      ExecutionLog.log("SHACLValidator#validate: loading Jena shapes model")
      val shapesModel = new RdflibRdfModel()
      new ValidationRdfModelEmitter(options.messageStyle.profileName, shapesModel).emit(shapes)

      validator.validateFromModels(
        dataModel.model,
        shapesModel.model, { (e: js.Dynamic, report: js.Dynamic) =>
          if (js.isUndefined(e) || e == null) {
            val result = new JSValidationReport(report)
            promise.success(result)
          } else {
            promise.failure(js.JavaScriptException(e))
          }
        }
      )

      promise.future
    } catch {
      case e: Exception =>
        promise.failure(e).future
    }
  }

  override def shapes(shapes: Seq[ValidationSpecification], functionsUrl: String): RdfModel = {
    val shapesModel = new RdflibRdfModel()
    new ValidationRdfModelEmitter(AmfProfile, shapesModel, functionsUrl).emit(shapes)
    shapesModel
  }

  override def emptyRdfModel(): RdfModel = new RdflibRdfModel()

  override def supportsJSFunctions: Boolean = true
}
