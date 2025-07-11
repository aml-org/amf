package amf.core.common

import org.mulesoft.common.io.{AsyncFile, FileSystem}
import org.mulesoft.common.test.Tests.checkDiff
import org.scalatest.Assertion

import scala.concurrent.Future

trait FileAssertionTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext {

  protected val fs: FileSystem = platform.fs

  protected def writeTemporaryFile(golden: String)(content: String): Future[AsyncFile] = {
    val file   = tmp(s"${golden.replaceAll("/", "-")}.tmp")
    val actual = fs.asyncFile(file)
    actual.write(content).map(_ => actual)
  }

  protected def assertDifferences(actual: AsyncFile, golden: String): Future[Assertion] = {
    val expected = fs.asyncFile(golden)
    expected.read().flatMap(_ => checkDiff(actual, expected))
  }

  /** Return random temporary file name for testing. */
  def tmp(name: String = ""): String =
    (platform.tmpdir() + platform.fs.separatorChar + System.nanoTime() + "-" + name)
      .replaceAll(s"${platform.fs.separatorChar}${platform.fs.separatorChar}", s"${platform.fs.separatorChar}")
}
