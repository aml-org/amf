package amf.shapes.internal.convert

import amf.core.client.platform.model.domain.RecursiveShape
import amf.core.internal.convert.UniqueInitializer
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.model.domain._
import amf.shapes.client.platform.model.domain.federation._
import amf.shapes.client.scala.model
import amf.shapes.internal.document.metamodel.DataTypeFragmentModel
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.metamodel.federation._
import amf.shapes.internal.domain.metamodel.operations._

/** Shared WebApi registrations. */
private[amf] object ShapesRegister extends UniqueInitializer with PlatformSecrets {

  // TODO ARM remove when APIMF-3000 is done
  def register(): Unit = register(platform)

  def register(platform: Platform): Unit = if (shouldInitialize) {

    // DataShapes (domain)
    platform.registerWrapper(AnyShapeModel) { case s: model.domain.AnyShape =>
      new AnyShape(s)
    }
    platform.registerWrapper(NilShapeModel) { case s: model.domain.NilShape =>
      NilShape(s)
    }
    platform.registerWrapper(ArrayShapeModel) { case s: model.domain.ArrayShape =>
      ArrayShape(s)
    }
    platform.registerWrapper(MatrixShapeModel) { case s: model.domain.MatrixShape =>
      new MatrixShape(s.toArrayShape)
    }
    platform.registerWrapper(TupleShapeModel) { case s: model.domain.TupleShape =>
      TupleShape(s)
    }
    platform.registerWrapper(CreativeWorkModel) { case s: model.domain.CreativeWork =>
      CreativeWork(s)
    }
    platform.registerWrapper(ExampleModel) { case s: model.domain.Example =>
      Example(s)
    }
    platform.registerWrapper(FileShapeModel) { case s: model.domain.FileShape =>
      FileShape(s)
    }
    platform.registerWrapper(NodeShapeModel) { case s: model.domain.NodeShape =>
      NodeShape(s)
    }
    platform.registerWrapper(DiscriminatorValueMappingModel) { case s: model.domain.DiscriminatorValueMapping =>
      DiscriminatorValueMapping(s)
    }
    platform.registerWrapper(DiscriminatorValueMappingModel) { case s: model.domain.DiscriminatorValueMapping =>
      DiscriminatorValueMapping(s)
    }
    platform.registerWrapper(ScalarShapeModel) { case s: model.domain.ScalarShape =>
      ScalarShape(s)
    }
    platform.registerWrapper(SchemaShapeModel) { case s: model.domain.SchemaShape =>
      SchemaShape(s)
    }
    platform.registerWrapper(XMLSerializerModel) { case s: model.domain.XMLSerializer =>
      XMLSerializer(s)
    }
    platform.registerWrapper(PropertyDependenciesModel) { case s: model.domain.PropertyDependencies =>
      PropertyDependencies(s)
    }
    platform.registerWrapper(SchemaDependenciesModel) { case s: model.domain.SchemaDependencies =>
      SchemaDependencies(s)
    }
    platform.registerWrapper(UnionShapeModel) { case s: model.domain.UnionShape =>
      UnionShape(s)
    }
    platform.registerWrapper(amf.core.internal.metamodel.domain.RecursiveShapeModel) {
      case s: amf.core.client.scala.model.domain.RecursiveShape => RecursiveShape(s)
    }
    platform.registerWrapper(IriTemplateMappingModel) { case s: model.domain.IriTemplateMapping =>
      IriTemplateMapping(s)
    }
    platform.registerWrapper(ShapePayloadModel) {
      case s: amf.shapes.client.scala.model.domain.operations.ShapePayload =>
        amf.shapes.client.platform.model.domain.operations.ShapePayload(s)
    }
    platform.registerWrapper(ShapeOperationModel) {
      case s: amf.shapes.client.scala.model.domain.operations.ShapeOperation =>
        amf.shapes.client.platform.model.domain.operations.ShapeOperation(s)
    }
    platform.registerWrapper(ShapeResponseModel) {
      case s: amf.shapes.client.scala.model.domain.operations.ShapeResponse =>
        amf.shapes.client.platform.model.domain.operations.ShapeResponse(s)
    }
    platform.registerWrapper(ShapeRequestModel) {
      case s: amf.shapes.client.scala.model.domain.operations.ShapeRequest =>
        amf.shapes.client.platform.model.domain.operations.ShapeRequest(s)
    }
    platform.registerWrapper(ShapeParameterModel) {
      case s: amf.shapes.client.scala.model.domain.operations.ShapeParameter =>
        amf.shapes.client.platform.model.domain.operations.ShapeParameter(s)
    }
    platform.registerWrapper(KeyModel) {
      case s: amf.shapes.client.scala.model.domain.federation.Key => Key(s)
    }
    platform.registerWrapper(PropertyKeyMappingModel) {
      case s: amf.shapes.client.scala.model.domain.federation.PropertyKeyMapping => PropertyKeyMapping(s)
    }
    platform.registerWrapper(ExternalPropertyShapeModel) {
      case s: amf.shapes.client.scala.model.domain.federation.ExternalPropertyShape => ExternalPropertyShape(s)
    }
  }

}
