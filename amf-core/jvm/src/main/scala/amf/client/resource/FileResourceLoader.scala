package amf.client.resource

import java.io.FileNotFoundException
import java.util.concurrent.CompletableFuture

import amf.client.remote.Content
import amf.core.lexer.FileStream
import amf.core.remote.FileNotFound
import amf.core.remote.FutureConverter._
import amf.core.remote.FileMediaType._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class FileResourceLoader() extends BaseFileResourceLoader {
  def fetchFile(resource: String): CompletableFuture[Content] = {
    Future {
      try {
        Content(new FileStream(resource),
                ensureFileAuthority(resource),
                extension(resource).flatMap(mimeFromExtension))
      } catch {
        case e: FileNotFoundException =>
          if (resource.contains("%20")) { // exception for local file system where we accept resources including spaces
            val escapedPath = resource.replace("%20", " ")
            try {
              Content(new FileStream(escapedPath),
                      ensureFileAuthority(resource),
                      extension(resource).flatMap(mimeFromExtension))
            } catch {
              case e: FileNotFoundException => throw FileNotFound(e)
            }
          } else {
            throw FileNotFound(e)
          }
      }
    }.asJava
  }

  def ensureFileAuthority(str: String): String = if (str.startsWith("file:")) str else s"file://$str"
}
