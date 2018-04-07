package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.Shape
import amf.core.services.PayloadValidator
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, ScalarShape}

class ExamplesValidationCollector(model: BaseUnit) {

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
              if example.fields
                .entry(ExampleModel.StructuredValue)
                .isDefined && example.strict.option().getOrElse(true) =>
            (shape, example)
        })
      case _ => Nil
    }
    allExamples
  }

  private def buildFragment(shape: Shape, example: Example) =
    PayloadFragment(example.structuredValue,
                    example.mediaType.option().getOrElse(PayloadValidator.guessMediaType(shape.isInstanceOf[ScalarShape], example.value.value())))

}

object ExamplesCandidatesCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] = new ExamplesValidationCollector(model).collect()
}
