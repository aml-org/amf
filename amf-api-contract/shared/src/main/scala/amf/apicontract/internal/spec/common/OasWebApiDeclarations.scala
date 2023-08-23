package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.FutureDeclarations
import org.yaml.model.YNode

class OasWebApiDeclarations(
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

object OasWebApiDeclarations {
  def apply(d: WebApiDeclarations): OasWebApiDeclarations = {
    val declarations = new OasWebApiDeclarations(
      Map(),
      d.alias,
      errorHandler = d.errorHandler,
      futureDeclarations = d.futureDeclarations
    )
    declarations.setLibraries(d.libraries)
    declarations.fragments = d.fragments
    declarations.shapes = d.shapes
    declarations.annotations = d.annotations
    declarations.resourceTypes = d.resourceTypes
    declarations.parameters = d.parameters
    declarations.payloads = d.payloads
    declarations.traits = d.traits
    declarations.securitySchemes = d.securitySchemes
    declarations.responses = d.responses
    declarations.annotations = d.annotations // FOR OAS -> RAML CONVERSION
    declarations                             // add withs methods?
  }
}
