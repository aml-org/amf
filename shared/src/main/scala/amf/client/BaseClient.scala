package amf.client

import amf.compiler.AMFCompiler
import amf.lexer.Token
import amf.parser.{ASTNode, Document}
import amf.remote.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

abstract class BaseClient {

  protected val remote: Platform

  def generate(url: String, syntax: String, handler: Handler): Unit = {
    AMFCompiler(url, remote).build().onComplete(callback(handler, url))
//    syntax match {
//      case "yaml" =>
//        YamlCompiler(url, remote).build().onComplete(callback(handler, url))
//      case "json" =>
//        JsonCompiler(url, remote).build().onComplete(callback(handler, url))
//    }
  }

  private def callback(handler: Handler, url: String)(t: Try[ASTNode[_ <: Token]]) = t match {
    case Success(value)     => handler.success(Document(value, url))
    case Failure(exception) => handler.error(exception)
  }
}
