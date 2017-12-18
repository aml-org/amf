package amf

import amf.client.commands.{CmdLineParser, ParseCommand, TranslateCommand, ValidateCommand}
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.unsafe.PlatformSecrets

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.Promise
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Main entry point for the application
  */
@JSExportTopLevel("Main")
@JSExportAll
object Main extends PlatformSecrets {

  def main(rawArgs: js.Array[String]): js.Promise[Any] = {
    val args = rawArgs.toArray
    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        cfg.mode match {
          case Some(ParserConfig.REPL)      =>
            println("REPL not supported in the JS client yet")
            failCommand()
            throw new Exception("Error executing AMF")
          case Some(ParserConfig.TRANSLATE) =>
            val f = runTranslate(cfg)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.VALIDATE)  =>
            val f = runValidate(cfg)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.PARSE)     =>
            val f = runParse(cfg)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case _                            =>
            failCommand()
            throw new Exception("Error executing AMF")
        }
      case _ =>
        js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
        throw new Exception("Error executing AMF")
    }
  }

  def failCommand(): Unit = {
    System.err.println("Wrong command")
    js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
  }
  def failPromise(e: Any): Unit = {
    System.err.println("Exception")
    System.err.println(e)
    js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
  }
  def runTranslate(config: ParserConfig): Future[Any] = TranslateCommand(platform).run(config)
  def runValidate(config: ParserConfig): Future[Any]  = ValidateCommand(platform).run(config)
  def runParse(config: ParserConfig): Future[Any]     = ParseCommand(platform).run(config)
}
