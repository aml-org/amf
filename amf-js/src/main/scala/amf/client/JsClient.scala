package amf.client

import amf.model.{BaseUnit, Document}
import amf.remote._
import amf.unsafe.{PlatformSecrets, TrunkPlatform}
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSConverters._

/**
  *
  */
@JSExportTopLevel("JsClient")
class JsClient extends BaseClient with PlatformSecrets with Client[BaseUnit] {

  /**
    * Converts the given api stream to a copy of the api in the spec of the vendor and runs the handler functions for success and fails
    * @param stream source api stream
    * @param sourceHint string that give a hint about the source spec and vendor. Ex: json, openapi, oas, raml, yaml
    * @param toVendor vendor for the result spec of the converted api. Ej: raml, oas, amf
    * @param handler handler object to execute the success or fail functions with the result string
    */
  @JSExport
  def convert(stream: String, sourceHint: String, toVendor: String, handler: JsHandler[String]): Unit = {

    generateFromStream(
      stream,
      matchSourceHint(sourceHint),
      new Handler[BaseUnit] {
        override def success(document: BaseUnit): Unit = {
          new JsGenerator().generateToString(
            document,
            matchToVendor(toVendor),
            new StringHandler {
              override def error(exception: Throwable): Unit = handler.error(exception)

              override def success(generation: String): Unit = handler.success(generation)
            }
          )
        }

        override def error(exception: Throwable): Unit = handler.error(exception)
      }
    )
  }

  /**
    * generates the base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  @JSExport
  override def generateFromFile(url: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generateAndHandle(url, hint, jsHander(handler))

  /**
    * generates the base unit document from given stream (api)
    * @param stream: the api stream
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    * @param handler handler object to execute the success or fail functions with the result object model
    */
  @JSExport
  override def generateFromStream(stream: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generateAndHandle(null, hint, jsHander(handler), Some(TrunkPlatform(stream)))

  /**
    * generates asynchronously base unit document from api located in the given file or url.
    * @param url : location of the api
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    */
  @JSExport
  def generateAsyncFromFile(url: String, hint: Hint): js.Promise[BaseUnit] =
    super.generate(url, hint).map(bu => Document(bu)).toJSPromise

  /**
    * generates asynchronously base unit document from given stream (api)
    * @param stream: the api stream
    * @param hint : hint that contains the spec and vendor to lexer and parse the api. Exp: OasJson, RamlYaml
    */
  @JSExport
  def generateAsyncFromStream(stream: String, hint: Hint): js.Promise[BaseUnit] =
    super.generate(null, hint, Some(TrunkPlatform(stream))).map(bu => Document(bu)).toJSPromise

  private def jsHander(handler: Handler[BaseUnit]) =
    new Handler[amf.document.BaseUnit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: amf.document.BaseUnit): Unit =
        handler.success(Document(document))
    }

}

@js.native
trait JsHandler[T] extends js.Object {
  def success(document: T): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
