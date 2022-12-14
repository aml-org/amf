package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.domain.ExternalSourceElementModel
import amf.core.internal.parser.YNodeLikeOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.domain.metamodel.ExampleModel
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YScalar, YSequence, YType}
import org.yaml.render.YamlRender

case class ExampleDataParser(entryLike: YMapEntryLike, example: Example, options: ExampleOptions)(implicit
    ctx: ShapeParserContext
) {
  private val node = entryLike.value
  def parse(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.setWithoutId(ExampleModel.Strict, AmfScalar(options.strictDefault), Annotations.synthesized())
    }

    val (targetNode, mutTarget) = node match {
      case mut: MutRef =>
        val refUrl = mut.origValue.asInstanceOf[YScalar].text
        ctx.fragments
          .get(refUrl)
          .foreach { e =>
            example.add(ExternalReferenceUrl(refUrl))
            example.callAfterAdoption { () => example.withReference(e.encoded.id) }
            example.set(ExternalSourceElementModel.Location, e.location.getOrElse(ctx.loc))
          }
        (mut.target.getOrElse(node), true)
      case _ =>
        (node, false) // render always (even if xml) for | multiline strings. (If set scalar.text we lose the token)

    }

    node.toOption[YScalar] match {
      case Some(value) if node.tagType == YType.Null =>
        if (isNullLiteral(value))
          example.setWithoutId(ExampleModel.Raw, AmfScalar("null"), Annotations.synthesized())
      case Some(scalar) =>
        example.setWithoutId(ExampleModel.Raw, AmfScalar(scalar.text), Annotations.synthesized())
      case _ =>
        example.set(ExampleModel.Raw, AmfScalar(YamlRender.render(targetNode)), Annotations.synthesized())
    }

    val result = NodeDataNodeParser(targetNode, example.id, options.quiet, mutTarget, options.isScalar).parse()

    result.dataNode.foreach { dataNode =>
      // If this example comes from a 08 param with type string, we force this to be a string
      example.set(ExampleModel.StructuredValue, dataNode, entryLike.fieldAnnotations)
    }

    example
  }

  private def isNullLiteral(value: YScalar) = value.text.nonEmpty
}

case class ExamplesDataParser(seq: YSequence, options: ExampleOptions, parentId: String)(implicit
    ctx: ShapeParserContext
) {
  def parse(): Seq[Example] = {
    val counter = new IdCounter()
    seq.nodes.map { n =>
      val exa = Example(n).withName(counter.genId("default-example"))
      ExampleDataParser(YMapEntryLike(n), exa, options).parse()
    }
  }
}
