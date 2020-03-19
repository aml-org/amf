package amf.client.commands

import amf.core.client.{ParserConfig, Proc, ProcWriter}
import amf.core.remote._
import amf.core.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class CommandLineTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  class TestWriter extends ProcWriter {
    var acc = ""

    override def print(s: String): Unit = acc += s

    override def print(e: Throwable): Unit = acc += e.toString

    override def toString(): String = acc
  }

  class TestProc extends Proc {
    var exitCode: Option[Int] = None

    override def exit(statusCode: Int): Unit = exitCode = Some(statusCode)

    def successful = exitCode.isEmpty
  }

  test("Parse command") {
    val args = Array("parse",
                     "-in",
                     Raml10.name,
                     "-mime-in",
                     "application/yaml",
                     "file://amf-client/shared/src/test/resources/upanddown/complete-with-operations.raml")
    val cfg = CmdLineParser.parse(args)
    assert(cfg.isDefined)
    val stdout = new TestWriter()
    val stderr = new TestWriter()
    val proc   = new TestProc()

    ParseCommand(platform).run(
      cfg.get.copy(
        stdout = stdout,
        stderr = stderr,
        proc = proc
      )) map { _ =>
      assert(stderr.acc == "")
      assert(stdout.acc != "")
      assert(proc.successful)
    }
  }

  test("Translate command") {
    val args = Array(
      "translate",
      "-in",
      Raml10.name,
      "-mime-in",
      "application/yaml",
      "-out",
      Oas20.name,
      "-mime-out",
      "application/json",
      "file://amf-client/shared/src/test/resources/upanddown/complete-with-operations.raml"
    )
    val cfg = CmdLineParser.parse(args)
    assert(cfg.isDefined)
    val stdout = new TestWriter()
    val stderr = new TestWriter()
    val proc   = new TestProc()

    TranslateCommand(platform).run(
      cfg.get.copy(
        stdout = stdout,
        stderr = stderr,
        proc = proc
      )) map { _ =>
      assert(stderr.acc == "")
      assert(stdout.acc != "")
      assert(proc.successful)
    }
  }

  test("Validation command") {
    val args = Array("validate",
                     "-in",
                     Raml10.name,
                     "-mime-in",
                     "application/yaml",
                     "-p",
                     Raml.name,
                     "file://amf-client/shared/src/test/resources/validations/data/error1.raml")
    val cfg = CmdLineParser.parse(args)
    assert(cfg.isDefined)
    assert(cfg.get.mode.get == ParserConfig.VALIDATE)
    val stdout = new TestWriter()
    val stderr = new TestWriter()
    val proc   = new TestProc()

    ValidateCommand(platform).run(
      cfg.get.copy(
        stdout = stdout,
        stderr = stderr,
        proc = proc
      )) map { _ =>
      assert(stderr.acc == "")
      assert(stdout.acc != "")
      assert(!proc.successful)
    }
  }

  test("Custom validation command") {
    val args = Array(
      "validate",
      "-in",
      Raml10.name,
      "-mime-in",
      "application/yaml",
      "-cp",
      "file://amf-client/shared/src/test/resources/validations/profiles/mercury.yaml",
      "file://amf-client/shared/src/test/resources/validations/mercury.raml"
    )
    val cfg = CmdLineParser.parse(args)
    assert(cfg.isDefined)
    assert(cfg.get.mode.get == ParserConfig.VALIDATE)
    val stdout = new TestWriter()
    val stderr = new TestWriter()
    val proc   = new TestProc()

    ValidateCommand(platform).run(
      cfg.get.copy(
        stdout = stdout,
        stderr = stderr,
        proc = proc
      )) map { _ =>
      assert(stderr.acc == "")
      assert(stdout.acc != "")
      assert(!proc.successful)
    }
  }

  test("Dialects parsing command") {
    val args = Array(
      "parse",
      "-in",
      Aml.name,
      "-mime-in",
      "application/yaml",
      "-ds",
      "file://amf-client/shared/src/test/resources/vocabularies2/production/k8/dialects/pod.raml",
      "file://amf-client/shared/src/test/resources/vocabularies2/production/k8/examples/pod.raml"
    )
    val cfg = CmdLineParser.parse(args)
    assert(cfg.isDefined)
    assert(cfg.get.mode.get == ParserConfig.PARSE)
    val stdout = new TestWriter()
    val stderr = new TestWriter()
    val proc   = new TestProc()

    ParseCommand(platform).run(
      cfg.get.copy(
        stdout = stdout,
        stderr = stderr,
        proc = proc
      )) map { _ =>
      assert(stderr.acc == "")
      assert(stdout.acc != "")
      assert(proc.successful)
    }
  }
}
