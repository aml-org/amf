package amf.client

import amf.model.{BaseUnit, Document}
import amf.remote.Hint
import amf.unsafe.TrunkPlatform

class JvmClient extends BaseClient with Client[BaseUnit] {

  override def generateFromFile(url: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generate(url, hint, jvmHander(handler))

  override def generateFromStream(stream: String, hint: Hint, handler: Handler[BaseUnit]): Unit =
    super.generate(null, hint, jvmHander(handler), Some(TrunkPlatform(stream)))

  def convert(stream: String, sourceHint: String, toVendor: String, handler: Handler[String]): Unit = {

    generateFromStream(
      stream,
      matchSourceHint(sourceHint),
      new Handler[BaseUnit] {
        override def success(document: BaseUnit): Unit = {
          new JvmGenerator().generateToString(
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

  def jvmHander(handler: Handler[BaseUnit]) =
    new Handler[amf.document.BaseUnit] {
      override def error(exception: Throwable): Unit = handler.error(exception)

      override def success(document: amf.document.BaseUnit): Unit =
        handler.success(Document(document.asInstanceOf[amf.document.Document]))
    }
}
