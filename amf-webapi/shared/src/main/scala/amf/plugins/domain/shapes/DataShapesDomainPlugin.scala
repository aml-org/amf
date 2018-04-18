package amf.plugins.domain.shapes

import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.core.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.plugins.domain.shapes.annotations.{InheritanceProvenance, InheritedShapes, ParsedFromTypeExpression}
import amf.plugins.domain.shapes.metamodel._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object DataShapesDomainPlugin extends AMFDomainPlugin {

  override val ID = "Data Shapes Domain"

  override def dependencies() = Seq()

  override def serializableAnnotations() = Map(
    "type-expression"        -> ParsedFromTypeExpression,
    "inheritance-provenance" -> InheritanceProvenance,
    "inherited-shapes"       -> InheritedShapes
  )

  override def modelEntities = Seq(
    AnyShapeModel,
    ArrayShapeModel,
    TupleShapeModel,
    MatrixShapeModel,
    FileShapeModel,
    NilShapeModel,
    NodeShapeModel,
    PropertyShapeModel,
    PropertyDependenciesModel,
    ScalarShapeModel,
    SchemaShapeModel,
    ShapeModel,
    UnionShapeModel,
    XMLSerializerModel,
    ShapeExtensionModel,
    ExampleModel
  )

  override def init(): Future[AMFPlugin] = Future { this }
}
