package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.model.domain.{ScalarNode, Shape}
import amf.core.utils._
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.{AnyShape, Example, ScalarShape}

import scala.collection.mutable

class ExamplesValidationCollector(model: BaseUnit) {

  val idCounter = new IdCounter()

  def collect(): Seq[ValidationCandidate] = {
    // we find all examples with strict validation
    findCandidates()
  }

  protected def findCandidates(): Seq[ValidationCandidate] = {
    val results = mutable.Map[String, Seq[Example]]()
    val shapes  = mutable.Map[String, Shape]()
    model.findByType((Namespace.Shapes + "Shape").iri()) foreach {
      case shape: AnyShape if results.keys.exists(_.equals(shape.id)) =>
        val currentExamples: Seq[Example] = results(shape.id)
        shape.examples.foreach({
          case example: Example
              if (example.fields.exists(ExampleModel.StructuredValue) || example.fields.exists(ExampleModel.Raw))
                && example.strict.option().getOrElse(true) && !currentExamples.exists(_.id.equals(example.id)) =>
            results.update(shape.id, currentExamples :+ example)
          case _ =>
        })
      case shape: AnyShape =>
        val examples = shape.examples.collect({
          case example: Example
              if (example.fields.exists(ExampleModel.StructuredValue) || example.fields.exists(ExampleModel.Raw))
                && example.strict.option().getOrElse(true) =>
            example
        })
        if (examples.nonEmpty) {
          results.put(shape.id, examples)
          shapes.put(shape.id, shape)
        }
      case _ =>
    }
    val seq = results
      .flatMap({
        case (id, e) =>
          val shape = shapes(id)
          e.map(e => ValidationCandidate(shape, buildFragment(shape, e)))
      })
      .toSeq
    seq
  }

  private def buildFragment(shape: Shape, example: Example) = {
    val mediaType = example.mediaType
      .option()
      .getOrElse(example.raw.value().guessMediaType(shape.isInstanceOf[ScalarShape]))
    val fragment =
      if (example.fields.exists(ExampleModel.StructuredValue))
        PayloadFragment(example.structuredValue, mediaType)
      else
        PayloadFragment(ScalarNode(example.raw.value(), None, example.annotations), mediaType) // todo: review with antonio

    fragment.encodes.withId(example.id)
    fragment
  }

}

object ExamplesCandidatesCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] = new ExamplesValidationCollector(model).collect()
}
