package amf.plugins.domain.shapes

import amf.client.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.core.annotations.{InheritanceProvenance, InheritedShapes, NilUnion}
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel._

import scala.concurrent.{ExecutionContext, Future}

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
    ExampleModel
  )

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future { this }
}
