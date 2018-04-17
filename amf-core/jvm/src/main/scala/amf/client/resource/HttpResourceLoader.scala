package amf.client.resource

import java.net.HttpURLConnection
import java.util.concurrent.CompletableFuture

import amf.client.remote.Content
import amf.core.lexer.CharArraySequence
import amf.core.remote.FutureConverter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class HttpResourceLoader() extends BaseHttpResourceLoader {
  override def fetch(resource: String): CompletableFuture[Content] = {
    val u          = new java.net.URL(resource)
    val connection = u.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    Future {
      connection.connect()
      connection.getResponseCode match {
        case 200 =>
          createContent(connection, resource)
        case s => throw new Exception(s"Unhandled status code $s => $resource")
      }
    }.asJava
  }

  private def createContent(connection: HttpURLConnection, url: String): Content = {
    new Content(
      CharArraySequence(connection.getInputStream, connection.getContentLength, None).toString,
      url,
      Option(connection.getHeaderField("Content-Type"))
    )
  }
}
