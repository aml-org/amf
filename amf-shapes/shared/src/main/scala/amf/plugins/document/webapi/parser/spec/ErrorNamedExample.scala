package amf.plugins.document.webapi.parser.spec

import amf.core.annotations.ErrorDeclaration
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YPart

case class ErrorNamedExample(idPart: String, ast: YPart)
    extends Example(Fields(), Annotations(ast))
    with ErrorDeclaration[ExampleModel.type] {
  override val namespace: String = "http://amferror.com/#errorNamedExample/"
  withId(idPart)

  override protected def newErrorInstance: ErrorDeclaration[ExampleModel.type] = ErrorNamedExample(idPart, ast)
  override val model: ExampleModel.type                                        = ExampleModel
}
