package amf.client

import amf.compiler.AMFCompiler
import amf.lexer.Token
import amf.maker.WebApiMaker
import amf.parser.{AMFUnit, ASTNode, Document}
import amf.remote.{Hint, RamlYamlHint, Vendor}
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

abstract class BaseClient extends PlatformSecrets {

  def generate(url: String, hint: Option[Hint], handler: Handler): Unit =
    AMFCompiler(url, platform, hint).build().onComplete(callback(handler, url))

  def webApiParserSuperCopado(): Unit = {
    AMFCompiler("file://shared/src/test/resources/webApi.raml", platform, Option(RamlYamlHint))
      .build()
      .onComplete(callback(
        new Handler {
          override def error(exception: Throwable): Unit = {}

          override def success(document: AMFUnit): Unit = {

            val webApi = new WebApiMaker(document).make

            println(webApi)
          }
        },
        ""
      ))
  }

  private def callback(handler: Handler, url: String)(t: Try[(ASTNode[_ <: Token], Vendor)]) = t match {
    case Success(value)     => handler.success(AMFUnit(value._1, url, Document, value._2)) //TODO dynamic vendor
    case Failure(exception) => handler.error(exception)
  }
}
