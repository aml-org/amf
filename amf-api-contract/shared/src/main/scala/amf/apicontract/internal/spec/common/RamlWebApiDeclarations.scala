package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.{DotQualifiedNameExtractor, FutureDeclarations}

class RamlWebApiDeclarations(
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations
) extends WebApiDeclarations(
      alias,
      errorHandler = errorHandler,
      futureDeclarations = futureDeclarations,
      DotQualifiedNameExtractor
    ) {

  def existsExternalAlias(lib: String): Boolean = externalLibs.contains(lib)

  def merge(other: RamlWebApiDeclarations): RamlWebApiDeclarations = {
    val merged =
      new RamlWebApiDeclarations(alias = alias, errorHandler = errorHandler, futureDeclarations = futureDeclarations)
    super.mergeParts(other, merged)
    externalShapes.foreach { case (k, s) => merged.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => merged.externalShapes += (k -> s) }
    merged
  }

  def absorb(other: RamlWebApiDeclarations): Unit = {
    super.mergeParts(other, this)
    externalShapes.foreach { case (k, s) => this.externalShapes += (k -> s) }
    other.externalShapes.foreach { case (k, s) => this.externalShapes += (k -> s) }
  }
}

object RamlWebApiDeclarations {
  def apply(d: WebApiDeclarations): RamlWebApiDeclarations = {
    val declarations = new RamlWebApiDeclarations(
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
    declarations // add withs methods?
  }
}
