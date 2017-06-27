package amf.client

import amf.compiler.AMFCompiler
import amf.lexer.Token
import amf.parser.{ASTNode, Container, Document}
import amf.remote.Hint
import amf.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

abstract class BaseClient extends PlatformSecrets {

  def generate(url: String, hint: Option[Hint], handler: Handler): Unit = {
    AMFCompiler(url, platform, hint).build().onComplete(callback(handler, url))
//    syntax match {
//      case "yaml" =>
//        YamlCompiler(url, remote).build().onComplete(callback(handler, url))
//      case "json" =>
//        JsonCompiler(url, remote).build().onComplete(callback(handler, url))
//    }
  }

  private def callback(handler: Handler, url: String)(t: Try[ASTNode[_ <: Token]]) = t match {
    case Success(value)     => handler.success(Container(value, url, Document))
    case Failure(exception) => handler.error(exception)
  }
}
