package amf.plugins.domain.shapes

import amf.plugins.domain.shapes.annotations.serializable.ShapeSerializableAnnotations
import amf.plugins.domain.shapes.entities.ShapeEntities

import scala.concurrent.{ExecutionContext, Future}

//object DataShapesDomainPlugin extends AMFDomainPlugin {
//
//  override val ID = "Data Shapes Domain"
//
//  override def dependencies() = Seq()
//
//  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = ShapeSerializableAnnotations.annotations
//
//  override def modelEntities: Seq[Obj] = ShapeEntities.entities.values.toSeq
//
//  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }
//}
