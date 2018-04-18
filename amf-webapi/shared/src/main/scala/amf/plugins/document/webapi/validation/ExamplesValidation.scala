package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.Shape
import amf.core.utils._
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.parser.spec.common.IdCounter
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, ScalarShape}

class ExamplesValidationCollector(model: BaseUnit) {

  val idCounter = new IdCounter()

  def collect(): Seq[ValidationCandidate] = {
    // we find all examples with strict validation
    val examples: Seq[(Shape, Example)] = findExamples()
    // We run regular payload validation for the supported examples
    examples.map { case (shape, example) => ValidationCandidate(shape, buildFragment(shape, example)) }
  }

  protected def findExamples(): Seq[(Shape, Example)] = {
    val allExamples: Seq[(Shape, Example)] = model.findByType((Namespace.Shapes + "Shape").iri()) flatMap {
      case shape: AnyShape =>
        shape.examples.collect({
          case example: Example
              if (example.fields.exists(ExampleModel.StructuredValue) || example.fields.exists(ExampleModel.Raw))
                && example.strict.option().getOrElse(true) =>
            (shape, example)
        })
      case _ => Nil
    }
    allExamples
  }

  private def buildFragment(shape: Shape, example: Example) = {
    val mediaType = example.mediaType
      .option()
      .getOrElse(example.raw.value().guessMediaType(shape.isInstanceOf[ScalarShape]))
    val fragment =
      if (example.fields.exists(ExampleModel.StructuredValue))
        PayloadFragment(example.structuredValue, mediaType)
      else
        PayloadFragment(example.raw.value(), mediaType) // todo: review with antonio

    fragment.encodes.withId(example.id)
    fragment
  }

}

object ExamplesCandidatesCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] = new ExamplesValidationCollector(model).collect()
}
