package amf.shapes.internal.spec.common.error

import amf.core.internal.annotations.ErrorDeclaration
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.domain.metamodel.ExampleModel
import org.yaml.model.YPart

case class ErrorNamedExample(idPart: String, ast: YPart)
    extends Example(Fields(), Annotations(ast))
    with ErrorDeclaration[ExampleModel.type] {
  override val namespace: String = "http://amferror.com/#errorNamedExample/"
  withId(idPart)

  override protected def newErrorInstance: ErrorDeclaration[ExampleModel.type] = ErrorNamedExample(idPart, ast)
  override val model: ExampleModel.type                                        = ExampleModel
}
