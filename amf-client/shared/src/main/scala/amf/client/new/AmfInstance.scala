package amf.client.`new`

import amf.ProfileName
import amf.client.`new`.amfcore.ResolutionPipeline
import amf.client.convert.CoreClientConverters.platform
import amf.client.parse.DefaultParserErrorHandler
import amf.core.client.ParsingOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeCompiler
import amf.core.validation.AMFValidationReport

import scala.concurrent.Future

class AmfInstance(env: AmfEnvironment,
                  errorHandlerProvider: ErrorHandlerProvider,
                  registry: AmfRegistry,
                  parsingOptions: ParsingOptions) {

  // como hacemos para devolver el ErrorHandler que nos genero si lo pide para un BU dado?
  // relacion EH vs BU? el BU DEBE tener el error handler adentro? (si no fue de parseo?)
  // el parse siempre devuelve error handler + base unit? (Amf result)

  // y el vendor? sobrecargamos el metodo de parse? un objeto aparte?
  def parse(url: String, vendor: Option[Vendor] = None): Future[AmfResult] = {
    val context = AmfParserContext(errorHandlerProvider.errorHandler(), env, registry, parsingOptions)
    RuntimeCompiler(
      url,
      None,
      vendor,
      Context(platform),
      context,
      cache = Cache()
    ) map { model =>
      model
    }
    // build parsing context?
  }

  def parse(uri: String, vendor: Vendor) = parse(uri, Some(vendor))

  def resolve(bu: BaseUnit): BaseUnit = {} // clone? BaseUnit.resolved
  def validate(bu: BaseUnit): AMFValidationReport // how we can handle the parsing validations? error handler at base unit?

}

sealed trait AmfContext {
  val env: AmfEnvironment
  val registry: AmfRegistry

}

case class AmfParserContext(errorHandler: ErrorHandler,
                            env: AmfEnvironment,
                            registry: AmfRegistry,
                            parsingOptions: ParsingOptions)
    extends AmfContext
