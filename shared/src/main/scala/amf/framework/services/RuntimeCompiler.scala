package amf.framework.services

import amf.document.BaseUnit
import amf.remote.Platform
import amf.validation.Validation

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RuntimeCompiler {
  def build(url: String,
            remote: Platform,
            mediaType: String,
            vendor: String,
            currentValidation: Validation): Future[BaseUnit]
}

object RuntimeCompiler {
  var compiler: Option[RuntimeCompiler] = None
  def register(runtimeCompiler: RuntimeCompiler) = {
    compiler = Some(runtimeCompiler)
  }

  def apply(url: String,
            remote: Platform,
            mediaType: String,
            vendor: String,
            currentValidation: Validation) = {
    compiler match {
      case Some(runtimeCompiler) => runtimeCompiler.build(url, remote, mediaType, vendor, currentValidation)
      case _                     => throw new Exception("No registered runtime compiler")
    }
  }
}
