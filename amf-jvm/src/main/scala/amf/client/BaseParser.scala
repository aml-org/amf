package amf.client

import java.util.concurrent.CompletableFuture

import amf.model.{BaseUnit, Document}
import amf.remote.FutureConverter.converters
import amf.remote.Syntax.Syntax
import amf.remote.Vendor
import amf.unsafe.TrunkPlatform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

/**
  * Base class for jvm parsers
  */
class BaseParser(protected val vendor: Vendor, protected val syntax: Syntax) extends PlatformParser {

  /**
    * generates the base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  def parseFile(url: String, handler: Handler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler))

  /**
    * generates the base unit document from given stream (api)
    * @param stream: the api stream
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  def parseString(stream: String, handler: Handler[BaseUnit]): Unit =
    super.parse(null, BaseUnitHandlerAdapter(handler), Some(TrunkPlatform(stream)))

  /**
    * generates asynchronously base unit document from api located in the given file or url.
    * @param url : location of the api
    */
  def parseFileAsync(url: String): CompletableFuture[BaseUnit] =
    super.parseAsync(url).map(bu => Document(bu)).asJava

  /**
    * generates asynchronously base unit document from given stream (api)
    * @param stream: the api stream
    */
  def parseStringAsync(stream: String): CompletableFuture[BaseUnit] =
    super.parseAsync(null, Some(TrunkPlatform(stream))).map(bu => Document(bu)).asJava

  private case class BaseUnitHandlerAdapter(handler: Handler[BaseUnit]) extends Handler[amf.document.BaseUnit] {
    override def success(document: amf.document.BaseUnit): Unit = handler.success(Document(document))
    override def error(exception: Throwable): Unit              = handler.error(exception)
  }
}
