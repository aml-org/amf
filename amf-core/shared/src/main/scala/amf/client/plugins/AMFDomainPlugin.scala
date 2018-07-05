package amf.client.plugins

import amf.core.metamodel.Obj
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.registries.AMFDomainEntityResolver

abstract class AMFDomainPlugin extends AMFPlugin {
  def modelEntities: Seq[Obj]
  def modelEntitiesResolver: Option[AMFDomainEntityResolver] = None
  def serializableAnnotations(): Map[String, AnnotationGraphLoader]
}
