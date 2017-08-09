package amf.client

import amf.model.{BaseUnit, Document}
import amf.remote._
import amf.unsafe.{PlatformSecrets, TrunkPlatform}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  *
  */
@JSExportTopLevel("JsClient")
class JsClient extends BaseClient with PlatformSecrets with Client[BaseUnit] {

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

  @JSExport
  override def generateFromFile(url: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generate(url, hint, jsHander(handler))

  def jsHander(handler: Handler[BaseUnit]) =
    new Handler[amf.document.BaseUnit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: amf.document.BaseUnit): Unit =
        handler.success(Document(document.asInstanceOf[amf.document.Document]))
    }

  @JSExport
  override def generateFromStream(stream: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generate(null, hint, jsHander(handler), Some(TrunkPlatform(stream)))

//  @JSExport
//  override def dumpToFile(baseUnit: BaseUnit, spec: Vendor, handler: Handler[Unit], platform: Platform, filePath: String): Unit =
//    super.dump(baseUnit.unit,spec, new Handler[String] {
//      override def error(exception: Throwable): Unit = handler.error(exception)
//
//      override def success(document: String): Unit = handler.success()
//    },Some(filePath))
//
//
//  @JSExport
//  override def dumpToStream(baseUnit: BaseUnit, spec: Vendor, handler: Handler[String]): Unit =
//    super.dump(baseUnit.unit,spec,new Handler[String] {
//      override def error(exception: Throwable): Unit = handler.error(exception)
//
//      override def success(document: String): Unit = handler.success(document)
//    })

}

@js.native
trait JsHandler[T] extends js.Object {
  def success(document: T): Unit = js.native

  def error(exception: Throwable): Unit = js.native
}
