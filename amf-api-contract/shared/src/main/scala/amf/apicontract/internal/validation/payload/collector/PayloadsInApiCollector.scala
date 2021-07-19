package amf.apicontract.internal.validation.payload.collector

import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{AmfElement, DataNode, ScalarNode, Shape}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Mimes._
import amf.core.internal.utils.MediaTypeMatcher
import amf.core.internal.validation.ValidationCandidate
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.client.scala.model.domain.{AnyShape, Example, ScalarShape}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ExampleModel}

object PayloadsInApiCollector extends ValidationCandidateCollector {

  private val anyShapeRestrictions =
    Seq(AnyShapeModel.Values,
        AnyShapeModel.Inherits,
        AnyShapeModel.Or,
        AnyShapeModel.And,
        AnyShapeModel.Xone,
        AnyShapeModel.Not)

  override def collect(element: AmfElement): Seq[ValidationCandidate] = {
    element match {
      case shape: AnyShape if isStrictAnyShape(shape) && !anyShapeRestrictions.exists(shape.fields.exists) => Nil
      // ignore any shape without logical restrictions, any payload it's valid
      case shape: AnyShape =>
        val collected = collectFromShape(shape)
        collected.map { encodes =>
          ValidationCandidate(shape, buildFragment(shape, encodes))
        }
      case _ => Nil
    }
  }

  private def isStrictAnyShape(shape: AnyShape) = shape.meta == AnyShapeModel

  private def collectFromShape(shape: AnyShape): Seq[CollectedElement] = {
    val examples = shape.examples.collect({
      case example: Example
          if example.fields.exists(ExampleModel.StructuredValue) && example.strict.option().getOrElse(true) =>
        DataNodeCollectedElement(example.structuredValue, example.id, example.raw.value(), example.annotations)
      case example: Example if example.fields.exists(ExampleModel.Raw) && example.strict.option().getOrElse(true) =>
        StringCollectedElement(example.id, example.raw.value(), example.annotations)
    })
    if (examples.nonEmpty) {
      examples ++ getDefault(shape)
    } else {
      getDefault(shape) match {
        case Some(ei) =>
          Seq(ei)
        case _ =>
          Nil
      }
    }
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
        PayloadFragment(dn.dataNode, `text/vnd.yaml`)
      case s: StringCollectedElement =>
        PayloadFragment(ScalarNode(s.raw, None, s.a), s.raw.guessMediaType(shape.isInstanceOf[ScalarShape])) // todo: review with antonio
    }
    fragment.encodes.setId(collectedElement.id)
    fragment
  }
}
