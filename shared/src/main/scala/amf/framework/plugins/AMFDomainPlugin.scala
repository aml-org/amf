package amf.framework.plugins

import amf.framework.model.domain.AnnotationGraphLoader

abstract class AMFDomainPlugin extends AMFPlugin {
  def serializableAnnotations(): Map[String, AnnotationGraphLoader]
}
