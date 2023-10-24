package amf.resolution.withrl

import amf.core.client.common.remote.Content
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.utils.AmfStrings
import org.mulesoft.common.io.{FileSystem, SyncFile}

import scala.collection.mutable
import scala.concurrent.Future

case class TestMapResourceLoader(files: Map[String, String]) extends ResourceLoader {

  override def fetch(resource: String): Future[Content] = {
    val s = sanitize(resource)
    files
      .get(s)
      .orElse(files.get(s.urlDecoded))
      .orElse(files.get(s.normalizePath))
      .orElse(files.get(s.urlDecoded.normalizePath.urlDecoded)) match {
      case Some(content) => Future.successful(new Content(content, s.normalizeUrl))
      case _             => Future.failed(new IllegalArgumentException(s"Resource '$resource' does not exist"))
    }
  }

  override def accepts(resource: String): Boolean = true

  private def sanitize(resource: String): String = {
    // Shrink file prefix (if any) and remove leading slashes (from includes with leading slashes),
    // as amf doesn't strip leading slashes and in the service we unzip all paths without leading slashes.
    val raw = resource.stripPrefix("file://").dropWhile(_ == '/')
    // Shrink all intermediate modules folders to make absolute loading of modules at root level (tooling behaviour)
    raw.substring(raw.lastIndexOf("/exchange_modules/") + 1)
  }
}

object TestMapResourceLoader {
  def apply(path: String)(implicit fs: FileSystem): TestMapResourceLoader = {
    val files = collectFiles(path).map { case (k, v) =>
      (k.stripPrefix(s"$path/"), v)
    }
    TestMapResourceLoader(files)
  }

  private def collectFiles(path: String)(implicit fs: FileSystem): Map[String, String] = {
    val filesMap    = mutable.Map[String, String]()
    val startingDir = fs.syncFile(path)
    if (startingDir.exists && startingDir.isDirectory) {
      collectRecursively(startingDir, filesMap)
    } else {
      println(s"The specified path '$path' is not a valid directory.")
    }

    filesMap.toMap
  }

  private def collectRecursively(dir: SyncFile, collector: mutable.Map[String, String])(implicit
      fs: FileSystem
  ): Unit = {
    dir.list.foreach { subPath =>
      val file = dir / subPath
      if (file.isDirectory) {
        collectRecursively(file, collector)
      } else {
        val content = file.read().toString
        collector += (file.path -> content)
      }
    }
  }
}
