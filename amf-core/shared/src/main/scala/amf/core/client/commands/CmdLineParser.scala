package amf.core.client.commands

import amf.ProfileNames
import amf.core.client.ParserConfig
import scopt.OptionParser

object CmdLineParser {

  def knownSpec(f: String) = {
    ProfileNames.RAML == f ||
    ProfileNames.OAS == f ||
    ProfileNames.AMF == f
  }

  val parser: OptionParser[ParserConfig] = new scopt.OptionParser[ParserConfig]("amf") {
    head("Application Modeling Framework", "1.0-pre")

    arg[String]("<file_in> [<file_out>]")
      .unbounded()
      .text("Input file to parse and output where translation will be stored")
      .action((f, c) => {
        if (c.input.isEmpty) c.copy(input = Some(f))
        else c.copy(output = Some(f))
      })

    opt[Seq[String]]("dialects")
      .valueName("<dialect_file1>,<dialect_file2>...")
      .abbr("ds")
      .text("List of dialects files that will loaded before parsing")
      .action((x, c) => c.copy(dialects = x))

    opt[String]("format-in")
      .abbr("in")
      .text("Input format for the file to parse")
      .validate({ f =>
        if (knownSpec(f)) {
          success
        } else {
          failure("Invalid value, values supported: RAML,OpenAPI,AMF")
        }
      })
      .action((f, c) => c.copy(inputFormat = Some(f)))

    opt[String]("validation-profile")
      .abbr("p")
      .text("Standard validation profile to use")
      .validate({ f =>
        if (knownSpec(f)) {
          success
        } else {
          failure("Invalid value, values supported: RAML,OpenAPI,AMF")
        }
      })
      .action((f, c) => c.copy(validationProfile = f))

    opt[String]("custom-validation-profile")
      .abbr("cp")
      .text("Custom validation profile location")
      .action((f, c) => c.copy(customProfile = Some(f)))

    cmd("repl")
      .text("Run in interactive mode")
      .action((_, c) => c.copy(mode = Some(ParserConfig.REPL)))

    cmd("parse")
      .text("Parse the input file and generates the JSON-LD AMF model")
      .action((_, c) => c.copy(mode = Some(ParserConfig.PARSE)))
      .children {

        opt[Boolean]("source-maps")
          .abbr("sm")
          .text("Generate source maps in AMF output")
          .action((f, c) => c.copy(withSourceMaps = f))

        opt[Boolean]("validate")
          .abbr("v")
          .text("Perform validation")
          .action((f, c) => c.copy(validate = f))
      }

    cmd("translate")
      .text("Translates the input file into a different format")
      .action((_, c) => c.copy(mode = Some(ParserConfig.TRANSLATE)))
      .children {

        opt[String]("format-out")
          .abbr("out")
          .text("Output format for the file to parse")
          .validate({ f =>
            if (knownSpec(f)) {
              success
            } else {
              failure("Invalid value, values supported: RAML,OpenAPI,AMF")
            }
          })
          .action((f, c) => c.copy(outputFormat = Some(f)))

        opt[Boolean]("source-maps")
          .abbr("sm")
          .text("Generate source maps in AMF output")
          .action((f, c) => c.copy(withSourceMaps = f))

        opt[Boolean]("validate")
          .abbr("v")
          .text("Perform validation")
          .action((f, c) => c.copy(validate = f))
      }

    cmd("validate")
      .text("Validates the spec and generates the validation report")
      .action((_, c) => c.copy(mode = Some(ParserConfig.VALIDATE)))

    checkConfig(c => {
      var error = ""
      if (c.input.isEmpty) error += "Missing <file_input>\n"
      if (c.inputFormat.isEmpty) error += "Missing --format-in\n"
      if (c.mode.isDefined && c.mode.get == ParserConfig.TRANSLATE) {
        if (c.outputFormat.isEmpty) error += "Missing --format-out\n"
      }
      if (error == "") success
      else failure(error)
    })

  }

  def parse(args: Array[String]): Option[ParserConfig] = parser.parse(args, ParserConfig())
}
