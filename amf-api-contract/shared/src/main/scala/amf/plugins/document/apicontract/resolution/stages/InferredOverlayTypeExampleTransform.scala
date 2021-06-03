package amf.plugins.document.apicontract.resolution.stages

import amf.client.remod.ParseConfiguration
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.{FieldEntry, ParserContext}
import amf.plugins.document.apicontract.contexts.parser.raml.Raml10WebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.document.apicontract.parser.spec.domain.NodeDataNodeParser
import amf.plugins.domain.shapes.metamodel.{ExampleModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models.{Example, ScalarShape}
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
      new Raml10WebApiContext("", Seq(), ParserContext(config = ParseConfiguration(errorHandler)))
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
