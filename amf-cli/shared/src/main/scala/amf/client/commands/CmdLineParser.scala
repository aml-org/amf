package amf.client.commands

import amf.core.client.common.validation._
import amf.core.internal.remote._
import scopt.OptionParser

object CmdLineParser {

  def knownSpec(f: String): Boolean = {
    Raml10Profile.profile == f ||
    Raml08Profile.profile == f ||
    Oas30Profile.profile == f ||
    Oas20Profile.profile == f ||
    AmfProfile.profile == f ||
    Aml.name == f

  }

  val parser: OptionParser[ParserConfig] = new scopt.OptionParser[ParserConfig]("amf") {
    head("Anything Modeling Framework", "4.X")

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
          failure(
            s"Invalid value $f, values supported: '${Raml10.name}', '${Raml08.name}', '${Oas20.name}', '${Aml.name}', '${Amf.name}'")
        }
      })
      .action((f, c) => c.copy(inputFormat = Some(f)))

    opt[String]("media-type-in")
      .abbr("mime-in")
      .text("Input media type for the file to parse")
      .action((f, c) => c.copy(inputMediaType = Some(f)))

    opt[String]("validation-profile")
      .abbr("p")
      .text("Standard validation profile to use")
      .validate({ f =>
        if (knownSpec(f)) {
          success
        } else {
          failure(
            s"Invalid value $f, values supported: '${Raml10.name}', '${Raml08.name}', '${Oas20.name}', '${Aml.name}', '${Amf.name}'")
        }
      })
      .action((f, c) => c.copy(validationProfile = f))

    opt[String]("custom-validation-profile")
      .abbr("cp")
      .text("Custom validation profile location")
      .action((f, c) => c.copy(customProfile = Some(f)))

    cmd("parse")
      .text("Parse the input file and generates the JSON-LD AMF model")
      .action((_, c) => c.copy(mode = Some(ParserConfig.PARSE)))
      .children {

        opt[Boolean]("source-maps")
          .abbr("sm")
          .text("Generate source maps in AMF output")
          .action((f, c) => c.copy(withSourceMaps = f))

        opt[Boolean]("compacted")
          .abbr("ctx")
          .text("Compact namespaces in context")
          .action((f, c) => c.copy(withCompactNamespaces = f))

        opt[Boolean]("validate")
          .abbr("v")
          .text("Perform validation")
          .action((f, c) => c.copy(validate = f))

        opt[Boolean]("resolve")
          .abbr("r")
          .text("Resolve after parsing")
          .action((f, c) => c.copy(resolve = f))

        opt[Boolean]("trace")
          .abbr("t")
          .text("Trace execution")
          .action { (f, c) =>
            println(s"TRACING EXECUTION... ${f} => ${c}")
            c.copy(trace = true)
          }
      }

    cmd("translate")
      .text("Translates the input file into a different format")
      .action((_, c) => c.copy(mode = Some(ParserConfig.TRANSLATE)))
      .children {

        opt[String]("format-out")
          .abbr("out")
          .text("Output format for the file to generate")
          .validate({ f =>
            if (knownSpec(f)) {
              success
            } else {
              failure(
                s"Invalid value $f, values supported: '${Raml10.name}', '${Raml08.name}', '${Oas20.name}', '${Aml.name}', '${Amf.name}'")
            }
          })
          .action((f, c) => c.copy(outputFormat = Some(f)))

        opt[String]("media-type-out")
          .abbr("mime-out")
          .text("Output media type for the file to generate")
          .action((f, c) => c.copy(outputMediaType = Some(f)))

        opt[Boolean]("source-maps")
          .abbr("sm")
          .text("Generate source maps in AMF output")
          .action((f, c) => c.copy(withSourceMaps = f))

        opt[Boolean]("validate")
          .abbr("v")
          .text("Perform validation")
          .action((f, c) => c.copy(validate = f))

        opt[Boolean]("resolve")
          .abbr("r")
          .text("Resolve after parsing")
          .action((f, c) => c.copy(resolve = f))
      }

    cmd("patch")
      .text("Apply a AML patch")
      .action((_, c) => c.copy(mode = Some(ParserConfig.PATCH)))
      .children {
        opt[String]("patch-target")
          .abbr("tg")
          .text("Location of the file being patched")
          .action((f, c) => c.copy(patchTarget = Some(f)))
      }

    cmd("validate")
      .text("Validates the spec and generates the validation report")
      .action((_, c) => c.copy(mode = Some(ParserConfig.VALIDATE)))

    checkConfig(c => {
      var error = ""
      if (c.input.isEmpty) error += "Missing <file_input>\n"
      if (c.inputMediaType.isEmpty) error += "Missing <file_input>\n"
      if (c.mode.isDefined && (c.mode.get != ParserConfig.PATCH)) {
        if (c.inputFormat.isEmpty) error += "Missing --format-in\n"
      }
      if (c.inputMediaType.isEmpty) error += "Missing --media-type-in\n"
      if (c.mode.isDefined && c.mode.get == ParserConfig.TRANSLATE) {
        if (c.outputFormat.isEmpty) error += "Missing --format-out\n"
        if (c.outputMediaType.isEmpty) error += "Missing --media-type-out\n"
      }
      if (error == "") success
      else failure(error)
    })

  }

  def parse(args: Array[String]): Option[ParserConfig] = parser.parse(args, ParserConfig())
}
