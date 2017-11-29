package amf.plugins.features

import amf.model.document.BaseUnit
import amf.plugins.features.validation.AMFValidatorPlugin

import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.Promise
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportAll
object AMFValidation {
  def init(): Promise[Any] = {
    AMFValidatorPlugin.init().toJSPromise
  }
}
