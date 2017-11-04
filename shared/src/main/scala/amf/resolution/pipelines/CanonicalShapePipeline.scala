package amf.resolution.pipelines
import amf.ProfileNames
import amf.document.BaseUnit
import amf.document.Fragment.DataType
import amf.domain.{Annotations, Fields}
import amf.metadata.document.FragmentModel
import amf.resolution.stages.{ReferenceResolutionStage, ShapeNormalizationStage}
import amf.shape.Shape

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
    val doc = new DataType(Fields(), Annotations())
    doc.fields.setWithoutId(FragmentModel.Encodes, shape)
    // get the canonical document with the canonical encoded shape
    val canonicalDoc = pipeline.resolve(doc)
    // extract the canonical shape
    canonicalDoc.fields.get(FragmentModel.Encodes).asInstanceOf[Shape]
  }
}
