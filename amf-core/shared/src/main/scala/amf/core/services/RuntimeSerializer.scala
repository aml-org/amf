package amf.core.services

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform

import scala.concurrent.Future

trait RuntimeSerializer {
  def dump(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): String
  def dumpToFile(platform: Platform, file: String, unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): Future[Unit]
}

object RuntimeSerializer {
  var serializer: Option[RuntimeSerializer] = None
  def register(runtimeSerializer: RuntimeSerializer) = {
    serializer = Some(runtimeSerializer)
  }

  def dumpToFile(platform: Platform, file: String, unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions) = {
    serializer match {
      case Some(runtimeSerializer) => runtimeSerializer.dumpToFile(platform, file, unit, mediaType, vendor, options)
      case None                    => throw new Exception("No registered runtime serializer")
    }
  }

  def apply(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions = GenerationOptions()): String = {
    serializer match {
      case Some(runtimeSerializer) => runtimeSerializer.dump(unit, mediaType, vendor, options)
      case None                    => throw new Exception("No registered runtime serializer")
    }
  }
}
