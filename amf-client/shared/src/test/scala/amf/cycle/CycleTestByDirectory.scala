package amf.cycle
import amf.client.parse.DefaultParserErrorHandler
import amf.core.remote.Syntax.Syntax
import amf.core.remote.{AmfJsonHint, Hint}
import amf.io.BuildCycleTests
import amf.plugins.document.graph.parser.{ExpandedForm, FlattenedForm, JsonLdDocumentForm, JsonLdSerialization, NoForm}
import org.mulesoft.common.io.{Fs, SyncFile}
import org.scalatest.{Assertion, AsyncFreeSpec}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Steps:
  *  1 - Simple cycle for <<directory>>: parse origin spec and generates target spec.
  *     Origin file: api.<<fileExtension>>
  *     Golden file: dumped.<<fileExtension>>
  *     Cannot be ignored
  *  2 - Cycle for golden <<directory>>: parse dumped (previous step golden) and generates the same target. Check the generated target.
  *     Origin file: dumped.<<fileExtension>>
  *     Golden file: dumped.<<fileExtension>>
  *     To ignore this step, add .ignore extension to dumped.<<fileExtension>> (Step 1 will run anyway)
  *  3 - Generate jsonld for <<directory>>: parse origin spec and generates a jsonld.
  *     Origin file: api.<<fileExtension>>
  *     Golden file: api.<<fileExtension>>.jsonld
  *     Cannot be ignored
  *  4 - Generate golden from jsonld for <<directory>>: parse the json for the origin and generates a target spec.
  *     Origin file: api.<<fileExtension>>.jsonld
  *     Golden file: api.<<fileExtension>>.jsonld.<<fileExtension>>
  *     To ignore this step, add .ignore extension to api.<<fileExtension>>.jsonld (Step 3 will run anyway)
  *  5 - Parse golden from jsonld for <<directory>>: parse dumped spec target generated from jsonld and generates the same target. Check the generated target throw jsonld.
  *     Origin file: api.<<fileExtension>>.jsonld.<<fileExtension>>
  *     Golden file: api.<<fileExtension>>.jsonld.<<fileExtension>>
  *     To ignore this step add .ignore extension to api.<<fileExtension>>.jsonlod.<<fileExtension>> (Setp 4 will run anyway, unless that be explicitly ignored).
  *
  * <<directory>> : each directory case in the basePath location.
  * <<fileExtension>>: the file extension provided for test.
  */
trait CycleTestByDirectory extends AsyncFreeSpec with BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def dirs: Array[SyncFile] =
    Fs.syncFile(basePath).list.map(l => Fs.syncFile(basePath + "/" + l)).partition(_.isFile)._2
  def origin: Hint
  def target: Hint // todo: multiple targets, one for each spec
  def fileExtension: String

  protected lazy val withEnableValidations: Seq[String] = Seq()

  dirs.foreach { d =>
    val dirName = d.name
    if (dirName.endsWith(".ignore")) {
      s"Source: $dirName" ignore { Future.successful(succeed) }
    } else {
      s"Source: $dirName" - {
        simpleCycle(dirName)

        goldenCycle(dirName, s"$dirName/dumped$fileExtension")

        s"Steps for json-ld serialization $dirName" - {
          val knownJsonLdForms = Seq(NoForm, FlattenedForm, ExpandedForm)
          knownJsonLdForms.foreach { form =>
            val jsonLdPath         = s"$dirName/api$fileExtension${form.extension}"
            val specFromJsonLdPath = s"$jsonLdPath$fileExtension"

            if (existGoldenForForm(d, form)) {
              val serialization = JsonLdSerialization(form)

              generateJsonLD(d, serialization)

              amfToSpec(d, serialization)

              specToAmfForAmf(dirName, specFromJsonLdPath)
            }
          }
        }
      }
    }
  }

  private def sanitize(path: String) = path.replace("//", "/")

  private def generateJsonLD(d: SyncFile, serialization: JsonLdSerialization): Unit = {
    val dirName = d.name
    s"Generate json-ld for $dirName with form ${serialization.form.name}" in {
      val jsonldExtension     = serialization.form.extension
      val additionalExtension = if (isIgnored(d, Some(jsonldExtension))) ".ignore" else ""
      val input               = s"$dirName/api.$fileExtension"
      val target              = s"$dirName/api.$fileExtension$jsonldExtension$additionalExtension"
      runCycle(input, target, origin, AmfJsonHint, None)
    }
  }

  private def isIgnored(d: SyncFile, additionalExtensionOpt: Option[String] = None): Boolean = {
    val additionalExtension = additionalExtensionOpt.getOrElse("")
    val path                = sanitize(s"$basePath/${d.name}/api.$fileExtension$additionalExtension.ignore")
    Fs.syncFile(path).exists
  }

  private def existGoldenForForm(d: SyncFile, form: JsonLdDocumentForm): Boolean = {
    val jsonldExtension = form.extension
    val path            = sanitize(s"$basePath/${d.name}/api.$fileExtension$jsonldExtension")
    Fs.syncFile(path).exists
  }

  private def goldenCycle(name: String, f: String): Unit = {
    if (Fs.syncFile(basePath + "/" + f + ".ignore").exists)
      s"Cycle for golden: $name" ignore {
        runCycle(f + ".ignore", target, Some(target.syntax))
      } else
      s"Cycle for golden: $name" in {
        runCycle(f, target, Some(target.syntax))
      }
  }

  private def simpleCycle(name: String): Unit = {
    val t = name + "/dumped" + fileExtension + (if (Fs.syncFile(
                                                        basePath + "/" + name + "/dumped" + fileExtension + ".ignore")
                                                      .exists) ".ignore"
                                                else "")
    s"Simple cycle for $name" in {
      runCycle(name + "/api" + fileExtension, t, origin, target, syntax = Some(target.syntax))
    }
  }

  private def amfToSpec(d: SyncFile, serialization: JsonLdSerialization): Unit = {
    val base = s"${d.name}/api$fileExtension"
    val jsonLdExtension =
      if (Fs.syncFile(s"$base${serialization.form.extension}").exists) {
        serialization.form.extension
      } else {
        // default extension
        NoForm.extension
      }
    amfToSpec(d.name, s"$base$jsonLdExtension", s"$base$jsonLdExtension$fileExtension")
  }

  private def amfToSpec(name: String, o: String, t: String): Unit = {
    val tar = if (Fs.syncFile(basePath + "/" + t + ".ignore").exists) t + ".ignore" else t

    if (Fs.syncFile(basePath + "/" + o + ".ignore").exists) {

      s"Generate golden from json-ld for $name" ignore {
        runCycle(o + ".ignore", tar, AmfJsonHint, target, syntax = Some(target.syntax))
      }
    } else {
      s"Generate golden from json-ld for $name" in {
        runCycle(o, tar, AmfJsonHint, target, syntax = Some(target.syntax))
      }
    }
  }

  private def specToAmfForAmf(name: String, f: String): Unit = {
    if (Fs.syncFile(basePath + "/" + f + ".ignore").exists) {
      s"Parse golden from json-ld for $name" ignore {
        runCycle(f + ".ignore", f + ".ignore", target, target, syntax = Some(target.syntax))
      }
    } else {
      s"Parse golden from json-ld for $name" in {
        runCycle(f, f, target, target, syntax = Some(target.syntax))
      }
    }

  }

  private def runCycle(source: String, target: Hint, syntax: Option[Syntax]): Future[Assertion] =
    runCycle(source, source, target, target, syntax)

  private def runCycle(source: String,
                       golden: String,
                       hint: Hint,
                       target: Hint,
                       syntax: Option[Syntax]): Future[Assertion] = {
    cycle(source,
          golden,
          hint,
          target.vendor,
          syntax = Some(target.syntax),
          eh = Some(DefaultParserErrorHandler.withRun()))
  }
}
