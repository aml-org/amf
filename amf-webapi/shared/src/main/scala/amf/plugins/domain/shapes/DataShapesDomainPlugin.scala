package amf.plugins.domain.shapes

import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.core.plugins.AMFDomainPlugin
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.metamodel._

object DataShapesDomainPlugin extends AMFDomainPlugin {

  override val ID = "Data Shapes Domain"

  override def dependencies() = Seq()

  override def serializableAnnotations() = Map(
    "type-exprssion" -> ParsedFromTypeExpression
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

}
