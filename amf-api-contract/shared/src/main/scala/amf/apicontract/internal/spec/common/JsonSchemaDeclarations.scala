package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.FutureDeclarations
import org.yaml.model.YNode

class JsonSchemaDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {}

object JsonSchemaDeclarations {
  def apply(d: WebApiDeclarations): JsonSchemaDeclarations = {
    val declarations = new JsonSchemaDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )

    declarations.shapes = d.shapes // Currently we will only support schema declarations
    declarations
  }
}
