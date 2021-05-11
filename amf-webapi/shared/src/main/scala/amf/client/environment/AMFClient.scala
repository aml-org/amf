package amf.client.environment

import amf.client.remod.{AMFParser, AMFResult, InvalidBaseUnitTypeException}
import amf.core.metamodel.document.{DocumentModel, ModuleModel}
import amf.core.model.document.{Document, Module}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The AMF Client contains common AMF operations.
  * For more complex uses see {@link amf.client.remod.AMFParser} or {@link amf.client.remod.AMFRenderer}
  */
class AMFClient private[amf] (override protected val configuration: AMFConfiguration)
    extends AMLClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.resolvers.executionContext.executionContext

  override def getConfiguration: AMFConfiguration = configuration

  /**
    * parse a {@link amf.core.model.document.Document}
    * @param url of the resource to parse
    * @return a Future{@link amf.client.environment.AMFDocumentResult}
    */
  def parseDocument(url: String): Future[AMFDocumentResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(d: Document, r) => new AMFDocumentResult(d, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.bu.meta, DocumentModel)
  }

  /**
    * parse a {@link amf.core.model.document.Module}
    * @param url of the resource to parse
    * @return a Future {@link amf.client.environment.AMFLibraryResult}
    */
  def parseLibrary(url: String): Future[AMFLibraryResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(m: Module, r) => new AMFLibraryResult(m, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.bu.meta, ModuleModel)
  }
}
