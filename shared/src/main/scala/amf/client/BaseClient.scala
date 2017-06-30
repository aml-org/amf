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

  private def callback(handler: Handler, url: String)(t: Try[(ASTNode[_ <: Token], Vendor)]) = t match {
    case Success(value)     => handler.success(AMFUnit(value._1, url, Document, value._2)) //TODO dynamic vendor
    case Failure(exception) => handler.error(exception)
  }
}
