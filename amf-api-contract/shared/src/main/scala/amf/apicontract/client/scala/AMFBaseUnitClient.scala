package amf.apicontract.client.scala

import amf.aml.client.scala.AMLBaseUnitClient
import amf.core.client.common.transform._
import amf.core.client.scala.AMFResult
import amf.core.client.scala.model.document.{BaseUnit, Document, Module}
import amf.core.client.scala.parse.{AMFParser, InvalidBaseUnitTypeException}
import amf.core.internal.metamodel.document.{DocumentModel, ModuleModel}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The AMF Client contains common AMF operations associated to base unit and documents.
  * For more complex uses see [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AMFBaseUnitClient private[amf] (override protected val configuration: AMFConfiguration)
    extends AMLBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: AMFConfiguration = configuration

  /**
    * parse a [[amf.core.client.platform.model.document.Document]]
    * @param url of the resource to parse
    * @return a Future [[AMFDocumentResult]]
    */
  def parseDocument(url: String): Future[AMFDocumentResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(d: Document, r) => new AMFDocumentResult(d, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, DocumentModel)
  }

  /**
    * parse a [[amf.core.client.scala.model.document.Module]]
    * @param url of the resource to parse
    * @return a Future [[AMFLibraryResult]]
    */
  def parseLibrary(url: String): Future[AMFLibraryResult] = AMFParser.parse(url, configuration).map {
    case AMFResult(m: Module, r) => new AMFLibraryResult(m, r)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, ModuleModel)
  }
}
