package amf.framework.plugins

import amf.framework.metamodel.Obj
import amf.framework.model.domain.AnnotationGraphLoader

abstract class AMFDomainPlugin extends AMFPlugin {
  def modelEntities: Seq[Obj]
  def serializableAnnotations(): Map[String, AnnotationGraphLoader]
}
