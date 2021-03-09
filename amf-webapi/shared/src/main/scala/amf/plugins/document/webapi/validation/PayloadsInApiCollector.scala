package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, DeclaresModel, PayloadFragment}
import amf.core.model.domain.{DataNode, ScalarNode, Shape}
import amf.core.parser.Annotations
import amf.core.utils._
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example, ScalarShape, UnionShape}
import amf.plugins.domain.shapes.validation.ShapesNodesValidator

import scala.collection.mutable

class PayloadsInApiCollector(model: BaseUnit) {

  val idCounter = new IdCounter()

  def collect(): Seq[ValidationCandidate] = {
    // we find all examples with strict validation
    findCandidates()
  }

  private def anyShapeRestrictions =
    Seq(AnyShapeModel.Values,
        AnyShapeModel.Inherits,
        AnyShapeModel.Or,
        AnyShapeModel.And,
        AnyShapeModel.Xone,
        AnyShapeModel.Not)

  protected def findCandidates(): Seq[ValidationCandidate] = {
    val candidateCollector = mutable.Map[String, Seq[CollectedElement]]()
    val shapesCollector    = mutable.Map[String, Shape]()

    model.iterator().foreach {
      case shape: AnyShape if shape.meta == AnyShapeModel && !anyShapeRestrictions.exists(shape.fields.exists) => // ignore any shape without logical restrictions, any payload it's valid
      case shape: AnyShape if candidateCollector.keys.exists(_.equals(shape.id)) =>
        collectFromExistingShape(candidateCollector, shape)
      case shape: AnyShape =>
        collectFromNewShape(candidateCollector, shapesCollector, shape)
      case _ =>
    }

    candidateCollector.flatMap {
      case (id, e) =>
        val shape = shapesCollector(id)
        if (e.isEmpty) {
          // this is the case of a shape that only has enums. A single validation candidate is created as all of its values
          // are then validated in ShapesNodesValidator::validateAll.
          Seq(
            ValidationCandidate(
              shape,
              PayloadFragment().withMediaType(ShapesNodesValidator.defaultMediaTypeFor(shape.values.head))))
        } else
          e.map { encodes =>
            ValidationCandidate(shape, buildFragment(shape, encodes))
          }
      case _ => Nil
    }.toSeq
  }

  private def collectFromNewShape(results: mutable.Map[String, Seq[CollectedElement]],
                                  shapes: mutable.Map[String, Shape],
                                  shape: AnyShape) = {
    val examples = shape.examples.collect({
      case example: Example
          if example.fields.exists(ExampleModel.StructuredValue) && example.strict.option().getOrElse(true) =>
        DataNodeCollectedElement(example.structuredValue, example.id, example.raw.value(), example.annotations)
      case example: Example if example.fields.exists(ExampleModel.Raw) && example.strict.option().getOrElse(true) =>
        StringCollectedElement(example.id, example.raw.value(), example.annotations)
    })
    if (examples.nonEmpty) {
      results.put(shape.id, examples ++ getDefault(shape))
      shapes.put(shape.id, shape)
    } else {
      getDefault(shape) match {
        case Some(ei) =>
          results.put(shape.id, Seq(ei))
          shapes.put(shape.id, shape)
        // first time i check the shape, i should collect it if has values
        case None if shape.values.nonEmpty =>
          shapes.put(shape.id, shape)
          results.put(shape.id, Nil)
        case _ => // ignore

      }
    }
  }
  private def collectFromExistingShape(results: mutable.Map[String, Seq[CollectedElement]], shape: AnyShape) = {
    val currentExamples: Seq[CollectedElement] = results(shape.id)
    shape.examples.foreach(e => {
      if (!currentExamples.exists(_.id.equals(e.id))) {
        e match {
          case example: Example
              if example.fields.exists(ExampleModel.StructuredValue)
                && example.strict.option().getOrElse(true) && !currentExamples.exists(_.id.equals(example.id)) =>
            results.update(shape.id,
                           currentExamples :+ DataNodeCollectedElement(example.structuredValue,
                                                                       example.id,
                                                                       example.raw.value(),
                                                                       example.annotations))
          case example: Example
              if example.fields.exists(ExampleModel.Raw)
                && example.strict.option().getOrElse(true) && !currentExamples.exists(_.id.equals(example.id)) =>
            results.update(
              shape.id,
              currentExamples :+ StringCollectedElement(example.id, example.raw.value(), example.annotations))
          case _ =>
        }
      }
    })
    results.update(shape.id, results(shape.id) ++ getDefault(shape))
  }
  private def getDefault(shape: Shape): Option[CollectedElement] = {
    Option(shape.default)
      .map(d => DataNodeCollectedElement(d, d.id, shape.defaultString.option().getOrElse(""), d.annotations))
      .orElse({
        shape.defaultString.option().map { s =>
          StringCollectedElement(shape.id, s, shape.defaultString.annotations())
        }
      })
  }

  private abstract class CollectedElement(val id: String, val raw: String, val a: Annotations)

  private case class DataNodeCollectedElement(dataNode: DataNode,
                                              override val id: String,
                                              override val raw: String,
                                              override val a: Annotations)
      extends CollectedElement(id, raw, a)

  private case class StringCollectedElement(override val id: String,
                                            override val raw: String,
                                            override val a: Annotations)
      extends CollectedElement(id, raw, a)

  private def buildFragment(shape: Shape, collectedElement: CollectedElement) = {
    val fragment = collectedElement match {
      case dn: DataNodeCollectedElement => // the example has been parsed, so i can use native validation like json or any default
        PayloadFragment(dn.dataNode, "text/vnd.yaml")
      case s: StringCollectedElement =>
        PayloadFragment(ScalarNode(s.raw, None, s.a), s.raw.guessMediaType(shape.isInstanceOf[ScalarShape])) // todo: review with antonio
    }
    fragment.encodes.withId(collectedElement.id)
    fragment
  }

}

object PayloadsInApiCollector {
  def apply(model: BaseUnit): Seq[ValidationCandidate] = new PayloadsInApiCollector(model).collect()
}
