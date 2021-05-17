package amf.plugins.domain.shapes.entities

import amf.core.entities.Entities
import amf.core.metamodel.Obj
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.plugins.domain.shapes.metamodel._

private[amf] object ShapeEntities extends Entities {

  override val innerEntities: Seq[Obj] = Seq(
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
