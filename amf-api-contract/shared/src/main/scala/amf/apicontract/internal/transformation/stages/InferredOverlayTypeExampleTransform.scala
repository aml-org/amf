package amf.apicontract.internal.transformation.stages

import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.raml.parser.context.Raml10WebApiContext
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.parser.LimitedParseConfig
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.SpecOrdering
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.client.scala.model.domain.{Example, ScalarShape}
import amf.shapes.internal.domain.metamodel.{ExampleModel, ScalarShapeModel}
import amf.shapes.internal.spec.common.emitter.DataNodeEmitter
import amf.shapes.internal.spec.common.parser.NodeDataNodeParser
import org.yaml.model.YDocument

sealed trait PreMergeTransform {
  def transform(main: DomainElement, overlay: DomainElement): DomainElement
}

class InferredOverlayTypeExampleTransform(implicit val errorHandler: AMFErrorHandler) extends PreMergeTransform {

  override def transform(main: DomainElement, overlay: DomainElement): DomainElement =
    (main, overlay, overlay.meta) match {
      case (_: Shape, overlayScalar: ScalarShape, ScalarShapeModel) if hasSynthesizedType(overlayScalar) =>
        transformExamplesOf(overlayScalar)
      case _ => overlay
    }

  private def transformExamplesOf(shape: ScalarShape): DomainElement = {
    val examples = shape.examples.map(e => transformStructuredValue(e))
    shape.withExamples(examples)
  }

  private def transformStructuredValue(example: Example): Example = {
    val node = YDocument({
      DataNodeEmitter(example.structuredValue, SpecOrdering.Lexical).emit(_)
    }).node
    // TODO: should be able to configure wether the DataNodeParser uses a ctx or not. Removing WebApiContext dependency from DataNodeParser is not a simple refactor.
    val dummyCtx =
      new Raml10WebApiContext("", Seq(), ParserContext(config = LimitedParseConfig(errorHandler)))
    val result = NodeDataNodeParser(node, example.id, quiet = true)(WebApiShapeParserContextAdapter(dummyCtx)).parse()
    result.dataNode.foreach { dataNode =>
      example.set(ExampleModel.StructuredValue, dataNode, example.structuredValue.annotations)
    }
    example
  }

  private def hasSynthesizedType(scalar: ScalarShape) = scalar.fields.entry(ScalarShapeModel.DataType) match {
    case Some(FieldEntry(_, value)) => value.isSynthesized
    case None                       => false
  }

}
