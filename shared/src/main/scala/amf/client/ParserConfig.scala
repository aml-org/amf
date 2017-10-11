package amf.client

import amf.ProfileNames

case class ParserConfig(mode: Option[String] = None,
                        input: Option[String] = None,
                        inputFormat: Option[String] = None,
                        output: Option[String] = None,
                        outputFormat: Option[String] = None,
                        withSourceMaps: Boolean = false,
                        validate: Boolean = true,
                        validationProfile: String = ProfileNames.AMF,
                        customProfile: Option[String] = None,
                        // list of dialects that will be loaded in the registry
                        // before parsing
                        dialects: Seq[String] = Seq()
                       )

object ParserConfig {
  val PARSE     = "parse"
  val TRANSLATE = "translate"
  val REPL      = "repl"
  val VALIDATE  = "validate"
}

object ExitCodes {
  val Success: Int = 0
  val WrongInvocation: Int = -1
  val FailingValidation: Int = -2
  val Exception: Int = -3
}