package amf.apicontract.internal.spec.common

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.FutureDeclarations
import org.yaml.model.YNode

class AwsOasWebApiDeclarations(
    override val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends OasWebApiDeclarations(
      asts,
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations
    ) {
  private var integrations: Map[String, DialectDomainElement] = Map.empty

  def findIntegration(name: String): Option[DialectDomainElement] = integrations.get(name)
  def addIntegration(name: String, integration: DialectDomainElement): AwsOasWebApiDeclarations = {
    integrations += (name -> integration)
    this
  }

}

object AwsOasWebApiDeclarations {
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
