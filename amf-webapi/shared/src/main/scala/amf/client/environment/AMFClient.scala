package amf.client.environment

import amf.client.remod.{AMFParser, AMFResult, InvalidBaseUnitTypeException}
import amf.core.metamodel.document.{DocumentModel, ModuleModel}
import amf.core.model.document.{Document, Module}

import scala.concurrent.{ExecutionContext, Future}

class AMFClient private[amf](override protected val configuration: AMFConfiguration)
    extends AMLClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.resolvers.executionContext.executionContext

  override def getConfiguration: AMFConfiguration = configuration

  def parseDocument(url: String): Future[AMFDocumentResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(d: Document, r) => new AMFDocumentResult(d, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.bu.meta, DocumentModel)
  }

  def parseLibrary(url: String): Future[AMFLibraryResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(m: Module, r) => new AMFLibraryResult(m, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.bu.meta, ModuleModel)
  }
}
