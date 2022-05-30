package amf.cli.internal.commands

import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.internal.unsafe.PlatformSecrets

abstract class ProcWriter {
  def print(s: String)
  def print(e: Throwable)
}

abstract class Proc {
  def exit(statusCode: Int)
}

object StdOutWriter extends ProcWriter with PlatformSecrets {
  override def print(s: String): Unit = platform.stdout(s)

  override def print(e: Throwable): Unit = platform.stdout(e)
}

object StdErrWriter extends ProcWriter {
  override def print(s: String): Unit = System.err.println(s)

  override def print(e: Throwable): Unit = System.err.println(e)
}

object RuntimeProc extends Proc {
  override def exit(statusCode: Int): Unit = System.exit(statusCode)
}

case class ParserConfig(
    mode: Option[String] = None,
    input: Option[String] = None,
    inputFormat: Option[String] = None,
    inputMediaType: Option[String] = None,
    output: Option[String] = None,
    outputFormat: Option[String] = None,
    outputMediaType: Option[String] = None,
    withSourceMaps: Boolean = false,
    withSourceInformation: Boolean = false,
    withCompactNamespaces: Boolean = false,
    validate: Boolean = true,
    trace: Boolean = false,
    patchTarget: Option[String] = None,
    validationProfile: String = AmfProfile.profile,
    customProfile: Option[String] = None,
    resolve: Boolean = false,
    // list of dialects that will be loaded in the registry
    // before parsing
    dialects: Seq[String] = Seq(),
    stdout: ProcWriter = StdOutWriter,
    stderr: ProcWriter = StdErrWriter,
    proc: Proc = RuntimeProc
) {

  val profile: ProfileName = ProfileName(validationProfile)
}

object ParserConfig {
  val PARSE     = "parse"
  val TRANSLATE = "translate"
  val REPL      = "repl"
  val VALIDATE  = "validate"
  val PATCH     = "patch"
}

object ExitCodes {
  val Success: Int           = 0
  val WrongInvocation: Int   = -1
  val FailingValidation: Int = -2
  val Exception: Int         = -3
}
