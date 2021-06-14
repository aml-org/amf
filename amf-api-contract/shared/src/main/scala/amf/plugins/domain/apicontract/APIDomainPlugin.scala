//package amf.plugins.domain.apicontract

//import amf.core.client.scala.model.domain.AnnotationGraphLoader
//import amf.core.internal.metamodel.Obj
//import amf.core.internal.plugins.AMFPlugin
//import amf.plugins.domain.apicontract.annotations.serializable.APISerializableAnnotations
//import amf.plugins.domain.apicontract.entities.APIEntities
//
//import scala.concurrent.{ExecutionContext, Future}

// TODO: restore legacy AMFDomainPlugin or remove this entirely.
//object APIDomainPlugin extends AMFDomainPlugin {
//
//  override val ID = "API Domain"
//
//  override def dependencies() = Seq(DataShapesDomainPlugin)
//
//  override def modelEntities: Seq[Obj] = APIEntities.entities.values.toSeq
//
//  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = APISerializableAnnotations.annotations
//
//  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }
//}
