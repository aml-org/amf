package amf.client

import amf.json.JsonCompiler
import amf.lexer.Token
import amf.parser.{ASTNode, Document}
import amf.remote.Platform
import amf.yaml.YamlCompiler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * Created by pedro.colunga on 5/29/17.
  */
abstract class BaseClient {

    protected def generate(url: String, syntax: String, remote: Platform, handler: Handler): Unit = {
        syntax match {
            case "yaml" =>
                YamlCompiler(url, remote).build().onComplete(callback(handler, url))
            case "json" =>
                JsonCompiler(url, remote).build().onComplete(callback(handler, url))
        }
    }

    private def callback(handler: Handler, url: String)(t : Try[ASTNode[_ <: Token]]) = t match {
            case Success(value) => handler.success(Document(value, url))
            case Failure(exception) => handler.error(exception)
    }
}
