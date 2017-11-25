package amf.core.plugins

import amf.core.metamodel.Obj
import amf.core.model.domain.AnnotationGraphLoader

abstract class AMFDomainPlugin extends AMFPlugin {
  def modelEntities: Seq[Obj]
  def serializableAnnotations(): Map[String, AnnotationGraphLoader]
}
