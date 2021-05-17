package amf.plugins.domain.webapi

import amf.client.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.core.metamodel.Obj
import amf.core.model.domain.AnnotationGraphLoader
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.annotations.serializable.APISerializableAnnotations
import amf.plugins.domain.webapi.entities.APIEntities

import scala.concurrent.{ExecutionContext, Future}

object APIDomainPlugin extends AMFDomainPlugin {

  override val ID = "API Domain"

  override def dependencies() = Seq(DataShapesDomainPlugin)

  override def modelEntities: Seq[Obj] = APIEntities.entities.values.toSeq

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = APISerializableAnnotations.annotations

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }
}
