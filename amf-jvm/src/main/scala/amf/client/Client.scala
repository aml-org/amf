package amf.client

import java.util.concurrent.CompletableFuture

import amf.client.FutureConverter.converters
import amf.model.{BaseUnit, Document}
import amf.remote.{Hint, Vendor}
import amf.unsafe.TrunkPlatform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class Client extends BaseClient {

  /**
    * generates the base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  def parseFromFile(url: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generateAndHandle(url, hint, jvmHander(handler))

  /**
    * generates the base unit document from given stream (api)
    * @param stream: the api stream
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  def parseFromStream(stream: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generateAndHandle(null, hint, jvmHander(handler), Some(TrunkPlatform(stream)))

  /**
    * generates asynchronously base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    */
  def parseFromFileAsync(url: String, hint: Hint): CompletableFuture[BaseUnit] =
    super.generate(url, hint).map(bu => Document(bu)).asJava

  /**
    * generates asynchronously base unit document from given stream (api)
    * @param stream: the api stream
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    */
  def parseFromStreamAsync(stream: String, hint: Hint): CompletableFuture[BaseUnit] =
    super.generate(null, hint, Some(TrunkPlatform(stream))).map(bu => Document(bu)).asJava

  /**
    * Converts the given api stream to a copy of the api in the spec of the vendor and runs the handler functions for success and fails
    * @param stream source api stream
    * @param hint string that give a hint about the source spec and vendor. Ex: json, openapi, oas, raml, yaml
    * @param target vendor for the result spec of the converted api. Ej: raml, oas, amf
    * @param handler handler object to execute the success or fail functions with the result string
    */
  def convert(stream: String, hint: Hint, target: Vendor, handler: StringHandler): Unit = {
    parseFromStream(
      stream,
      hint,
      new Handler[BaseUnit] {
        override def success(document: BaseUnit): Unit =
          new Generator().generateString(document, target, handler)
        override def error(exception: Throwable): Unit = handler.error(exception)
      }
    )
  }

  private def jvmHander(handler: Handler[BaseUnit]) =
    new Handler[amf.document.BaseUnit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: amf.document.BaseUnit): Unit = handler.success(Document(document))
    }
}
