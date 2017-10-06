package amf.client

import amf.client.commands.{CmdLineParser, ParseCommand, TranslateCommand, ValidateCommand}
import amf.unsafe.PlatformSecrets

import scala.concurrent.Future
import scalajs.js
import js.JSConverters._

/**
  * Main entry point for the application
  */
object Main extends PlatformSecrets {

  // Nothing is passed in the main args in ScalaJS
  def main(a: Array[String]): Unit = {
    // I need to reconstruct the args using Node process
    val args = js.Dynamic.global.process.argv.slice(2).asInstanceOf[js.Array[String]].toArray
    CmdLineParser.parse(args.asInstanceOf[Array[String]]) match {
      case Some(cfg) =>
        cfg.mode match {
          case Some(ParserConfig.REPL)      => runRepl()
          case Some(ParserConfig.TRANSLATE) => runTranslate(cfg)
          case Some(ParserConfig.VALIDATE)  => runValidate(cfg)
          case Some(ParserConfig.PARSE)     => runParse(cfg)
          case _                            => failCommand()
        }
      case _ => platform.exit(ExitCodes.WrongInvocation)
    }
  }

  def failCommand() = {
    System.err.println("Wrong command")
    platform.exit(ExitCodes.WrongInvocation)
  }

  def runRepl()                                       = throw new Exception("REPL not available yet in Node version")
  def runTranslate(config: ParserConfig): Future[Any] = TranslateCommand(platform).run(config)
  def runValidate(config: ParserConfig): Future[Any]  = ValidateCommand(platform).run(config)
  def runParse(config: ParserConfig): Future[Any]     = ParseCommand(platform).run(config)

}
