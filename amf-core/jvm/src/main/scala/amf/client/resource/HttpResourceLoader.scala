package amf.client.resource

import java.net.{HttpURLConnection, SocketTimeoutException}
import java.util.concurrent.CompletableFuture

import amf.client.remote.Content
import amf.core.lexer.CharArraySequence
import amf.core.remote.FutureConverter._
import amf.core.remote.SocketTimeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class HttpResourceLoader() extends BaseHttpResourceLoader {
  override def fetch(resource: String): CompletableFuture[Content] = {
    val u          = new java.net.URL(resource)
    val connection = u.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setConnectTimeout(System.getProperty("amf.connection.connect.timeout", "5000").toInt)
    connection.setReadTimeout(System.getProperty("amf.connection.read.timeout", "60000").toInt)

    Future {
      try {
        connection.connect()
        connection.getResponseCode match {
          case 200 =>
            createContent(connection, resource)
          case s => throw new Exception(s"Unhandled status code $s => $resource")
        }
      } catch {
        case e: SocketTimeoutException => throw SocketTimeout(e)
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
