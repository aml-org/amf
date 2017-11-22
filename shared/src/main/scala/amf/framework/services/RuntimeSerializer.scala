package amf.framework.services

import amf.client.GenerationOptions
import amf.document.BaseUnit

trait RuntimeSerializer {
  def dump(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): String
}

object RuntimeSerializer {
  var serializer: Option[RuntimeSerializer] = None
  def register(runtimeSerializer: RuntimeSerializer) = {
    serializer = Some(runtimeSerializer)
  }

  def apply(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions = GenerationOptions()) = {
    serializer match {
      case Some(runtimeSerializer) => runtimeSerializer.dump(unit, mediaType, vendor, options)
      case None                    => throw new Exception("No registered runtime serializer")
    }
  }
}
