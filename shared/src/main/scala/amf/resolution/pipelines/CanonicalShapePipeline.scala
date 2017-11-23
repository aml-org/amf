package amf.resolution.pipelines
import amf.ProfileNames
import amf.framework.model.document.BaseUnit
import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.framework.metamodel.document.FragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.domain.shapes.models.Shape
import amf.resolution.stages.{ReferenceResolutionStage, ShapeNormalizationStage}

class CanonicalShapePipeline extends ResolutionPipeline {

  val references = new ReferenceResolutionStage(ProfileNames.AMF)
  val shapes     = new ShapeNormalizationStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      step(references)
      step(shapes)
    }
  }
}

object CanonicalShapePipeline {
  def apply(shape: Shape): Shape = {
    // create the pipeline
    val pipeline = new CanonicalShapePipeline()
    // encode the shape in a doc to apply the pipeline
    val doc = new DataTypeFragment(Fields(), Annotations())
    doc.fields.setWithoutId(FragmentModel.Encodes, shape)
    // get the canonical document with the canonical encoded shape
    val canonicalDoc = pipeline.resolve(doc)
    // extract the canonical shape
    canonicalDoc.fields.get(FragmentModel.Encodes).asInstanceOf[Shape]
  }
}
