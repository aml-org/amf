package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.FutureDeclarations
import org.yaml.model.YNode

class AsyncWebApiDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasLikeWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {}

object AsyncWebApiDeclarations {
  def apply(d: WebApiDeclarations): AsyncWebApiDeclarations = {
    val declarations = new AsyncWebApiDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )

    // TODO ASYNC complete this
    declarations.securitySchemes = d.securitySchemes
    declarations
  }
}
