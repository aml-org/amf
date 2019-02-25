package amf.plugins.domain.shapes

import amf.core.metamodel.domain._
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.client.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.plugins.domain.shapes.annotations.{
  InheritanceProvenance,
  InheritedShapes,
  NilUnion,
  ParsedFromTypeExpression
}
import amf.plugins.domain.shapes.metamodel._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object DataShapesDomainPlugin extends AMFDomainPlugin {

  override val ID = "Data Shapes Domain"

  override def dependencies() = Seq()

  override def serializableAnnotations() = Map(
    "type-expression"        -> ParsedFromTypeExpression,
    "inheritance-provenance" -> InheritanceProvenance,
    "inherited-shapes"       -> InheritedShapes,
    "nil-union"              -> NilUnion
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
    UnionShapeModel,
    XMLSerializerModel,
    ShapeExtensionModel,
    ExampleModel,
    ObjectNodeModel,
    ScalarNodeModel,
    ArrayNodeModel,
    LinkNodeModel,
    RecursiveShapeModel
  )

  override def init(): Future[AMFPlugin] = Future { this }
}
