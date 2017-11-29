package amf.plugins.features

import java.util.concurrent.CompletableFuture

import amf.plugins.features.validation.AMFValidatorPlugin
import amf.core.remote.FutureConverter._

import scala.concurrent.ExecutionContext.Implicits.global

object AMFValidation {
  def init(): CompletableFuture[Any] = {
    AMFValidatorPlugin.init().asJava
  }
}
