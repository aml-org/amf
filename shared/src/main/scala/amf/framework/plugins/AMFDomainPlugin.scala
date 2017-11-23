package amf.framework.plugins

import amf.framework.domain.AnnotationGraphLoader

abstract class AMFDomainPlugin extends AMFPlugin {
  def serializableAnnotations(): Map[String, AnnotationGraphLoader]
}
