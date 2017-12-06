package amf.plugins.domain

import amf.core.unsafe.PlatformSecrets
import amf.model.domain._
import amf.plugins.domain.shapes.{DataShapesDomainPlugin, metamodel, models}

object DataShapes extends PlatformSecrets {
  def register() = {
    platform.registerWrapper(metamodel.AnyShapeModel) {
      case s: models.AnyShape => new AnyShape(s)
    }
    platform.registerWrapper(metamodel.NilShapeModel) {
      case s: models.NilShape => NilShape(s)
    }
    platform.registerWrapper(metamodel.ArrayShapeModel) {
      case s: models.ArrayShape => ArrayShape(s)
    }
    platform.registerWrapper(metamodel.MatrixShapeModel) {
      case s: models.MatrixShape => MatrixShape(s)
    }
    platform.registerWrapper(metamodel.TupleShapeModel) {
      case s: models.TupleShape => TupleShape(s)
    }
    platform.registerWrapper(metamodel.CreativeWorkModel) {
      case s: models.CreativeWork => CreativeWork(s)
    }
    platform.registerWrapper(metamodel.ExampleModel) {
      case s: models.Example => Example(s)
    }
    platform.registerWrapper(metamodel.FileShapeModel) {
      case s: models.FileShape => FileShape(s)
    }
    platform.registerWrapper(metamodel.NodeShapeModel) {
      case s: models.NodeShape => NodeShape(s)
    }
    platform.registerWrapper(metamodel.ScalarShapeModel) {
      case s: models.ScalarShape => ScalarShape(s)
    }
    platform.registerWrapper(metamodel.SchemaShapeModel) {
      case s: models.SchemaShape => SchemaShape(s)
    }
    platform.registerWrapper(metamodel.XMLSerializerModel) {
      case s: models.XMLSerializer => XMLSerializer(s)
    }
    platform.registerWrapper(metamodel.PropertyDependenciesModel) {
      case s: models.PropertyDependencies => PropertyDependencies(s)
    }

    // plugin initialization
    amf.Core.registerPlugin(DataShapesDomainPlugin)
  }
}
