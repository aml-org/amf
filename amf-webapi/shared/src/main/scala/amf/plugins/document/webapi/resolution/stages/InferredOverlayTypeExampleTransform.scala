package amf.plugins.document.webapi.resolution.stages

import amf.core.annotations.LexicalInformation
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{FieldEntry, ParserContext}
import amf.core.validation.AMFValidationResult
import amf.plugins.document.webapi.contexts.parser.raml.Raml10WebApiContext
import amf.plugins.document.webapi.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.domain.shapes.metamodel.{ExampleModel, ScalarShapeModel}
import amf.plugins.domain.shapes.models.{Example, ScalarShape}
import org.yaml.model.YDocument

sealed trait PreMergeTransform {
  def transform(main: DomainElement, overlay: DomainElement): DomainElement
}

class InferredOverlayTypeExampleTransform(implicit val errorHandler: ErrorHandler) extends PreMergeTransform {

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
      new Raml10WebApiContext("", Seq(), ParserContext(eh = new ParserErrorHandlerAdapter(errorHandler)))
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

  class ParserErrorHandlerAdapter(errorHandler: ErrorHandler) extends ParserErrorHandler {
    override def reportConstraint(id: String,
                                  node: String,
                                  property: Option[String],
                                  message: String,
                                  lexical: Option[LexicalInformation],
                                  level: String,
                                  location: Option[String]): Unit =
      errorHandler.reportConstraint(id, node, property, message, lexical, level, location)

    override def results(): List[AMFValidationResult] = errorHandler.results()
  }

}
