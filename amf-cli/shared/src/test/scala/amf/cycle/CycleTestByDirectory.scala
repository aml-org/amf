package amf.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.internal.plugins.document.graph._
import amf.core.internal.remote.{AmfJsonHint, Hint}
import amf.io.{BuildCycleTests, JsonLdSerializationSuite}
import org.mulesoft.common.io.{Fs, SyncFile}
import org.scalatest.Assertion

import scala.concurrent.Future

/** Steps:
  *   1. Simple cycle for [[directory]]: parse origin spec and generates target spec. Origin file: api.[[fileExtension]]
  *      Golden file: dumped.[[fileExtension]] Cannot be ignored
  *
  *   1. Cycle for golden [[directory]]: parse dumped (previous step golden) and generates the same target. Check the
  *      generated target. Origin file: dumped.[[fileExtension]] Golden file: dumped.[[fileExtension]] To ignore this
  *      step, add .ignore extension to dumped.[[fileExtension]] (Step 1 will run anyway)
  *
  *   1. Generate jsonld for [[directory]]: parse origin spec and generates a jsonld. Origin file: api.[[fileExtension]]
  *      Golden file: api.[[fileExtension]].jsonld Cannot be ignored
  *
  *   1. Generate golden from jsonld for [[directory]]: parse the jsonld for the origin and generates a target spec.
  *      Origin file: api.[[fileExtension]].jsonld Golden file: api.[[fileExtension]].jsonld.[[fileExtension]] To ignore
  *      this step, add .ignore extension to api.[[fileExtension]].jsonld (Step 3 will run anyway)
  *
  *   1. Parse golden from jsonld for [[directory]]: parse dumped spec target generated from jsonld and generates the
  *      same target. Check the generated target throw jsonld. Origin file:
  *      api.[[fileExtension]].jsonld.[[fileExtension]] Golden file: api.[[fileExtension]].jsonld.[[fileExtension]] To
  *      ignore this step add .ignore extension to api.[[fileExtension]].jsonld.[[fileExtension]] (Step 4 will run
  *      anyway, unless that be explicitly ignored).
  *
  * [[directory]] : each directory case in the basePath location. [[fileExtension]]: the file extension provided for
  * test.
  */
trait CycleTestByDirectory extends BuildCycleTests with JsonLdSerializationSuite {

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  def dirs: Array[SyncFile] =
    Fs.syncFile(basePath).list.map(l => Fs.syncFile(basePath + "/" + l)).partition(_.isFile)._2

  def origin: Hint

  def target: Hint // todo: multiple targets, one for each spec

  def fileExtension: String

  protected lazy val withEnableValidations: Seq[String] = Seq()

  dirs.foreach { d =>
    val dirName = d.name
    if (dirName.endsWith(".ignore")) {
      ignore(s"Source: $dirName") {
        Future.successful(succeed)
      }
    } else {

      simpleCycle(dirName)

      goldenCycle(dirName)

      val knownJsonLdForms = Seq(NoForm, FlattenedForm, EmbeddedForm)

      knownJsonLdForms.foreach { form =>
        if (existGoldenForForm(d, form)) {
          val serialization = JsonLdSerialization(form)

          generateJsonLD(d, serialization)

          amfToSpec(d, serialization)

          specToAmfForAmf(dirName, form)
        }
      }

    }
  }

  private def sanitize(path: String) = path.replace("//", "/")

  private def generateJsonLD(d: SyncFile, serialization: JsonLdSerialization): Unit = {
    val dirName = d.name
    test(s"Generate json-ld for $dirName with form ${serialization.form.name}") {
      val jsonldExtension     = serialization.form.extension
      val additionalExtension = if (isIgnored(d, Some(jsonldExtension))) ".ignore" else ""
      val input               = s"$dirName/api$fileExtension"
      val target              = s"$dirName/api$fileExtension.$jsonldExtension$additionalExtension"
      runCycle(input, target, origin, AmfJsonHint, Some(renderOptionsFor(serialization.form)))
    }
  }

  private def isIgnored(d: SyncFile, additionalExtensionOpt: Option[String] = None): Boolean = {
    val additionalExtension = additionalExtensionOpt.getOrElse("")
    val path                = sanitize(s"$basePath/${d.name}/api.$fileExtension$additionalExtension.ignore")
    Fs.syncFile(path).exists
  }

  private def existGoldenForForm(d: SyncFile, form: JsonLdDocumentForm): Boolean = {
    val jsonldExtension = form.extension
    val path            = sanitize(s"$basePath/${d.name}/api$fileExtension.$jsonldExtension")
    Fs.syncFile(path).exists
  }

  private def goldenCycle(name: String): Unit = {
    val f = s"$name/dumped$fileExtension"
    if (Fs.syncFile(basePath + "/" + f + ".ignore").exists)
      ignore(s"Cycle for golden: $name") {
        runCycle(f + ".ignore", target)
      }
    else
      test(s"Cycle for golden: $name") {
        runCycle(f, target)
      }
  }

  private def simpleCycle(dirname: String): Unit = {
    val t =
      dirname + "/dumped" + fileExtension + (if (
                                            Fs.syncFile(basePath + "/" + dirname + "/dumped" + fileExtension + ".ignore")
                                              .exists
                                          ) ".ignore"
                                          else "")
    test(s"Simple cycle for $dirname") {
      runCycle(dirname + "/api" + fileExtension, t, origin, target, None)
    }
  }

  private def amfToSpec(d: SyncFile, serialization: JsonLdSerialization): Unit = {
    val base = s"${d.name}/api$fileExtension"

    val source = {
      if (Fs.syncFile(s"$basePath$base.${serialization.form.extension}").exists) {
        s"$base.${serialization.form.extension}" // form specific source
      } else {
        s"$base.${NoForm.extension}" // default source
      }
    }

    val target = {
      if (Fs.syncFile(s"$basePath$base.${serialization.form.extension}$fileExtension").exists) {
        s"$base.${serialization.form.extension}$fileExtension" // form specific source
      } else {
        s"$base.${NoForm.extension}$fileExtension" // default source
      }
    }

    amfToSpec(d.name, source, target, serialization)
  }

  private def amfToSpec(name: String, o: String, t: String, serialization: JsonLdSerialization): Unit = {
    val tar = if (Fs.syncFile(basePath + "/" + t + ".ignore").exists) t + ".ignore" else t

    if (Fs.syncFile(basePath + "/" + o + ".ignore").exists) {
      ignore(s"Generate golden from json-ld for $name with form ${serialization.form.name}") {
        runCycle(o + ".ignore", tar, AmfJsonHint, target, None)
      }
    } else {
      test(s"Generate golden from json-ld for $name with form ${serialization.form.name}") {
        runCycle(o, tar, AmfJsonHint, target, None)
      }
    }
  }

  private def specToAmfForAmf(name: String, form: JsonLdDocumentForm): Unit = {
    val jsonLdPath = s"$name/api$fileExtension."
    val f = {
      val specificPath = s"$jsonLdPath${form.extension}$fileExtension"
      val defaultPath  = s"$jsonLdPath${NoForm.extension}$fileExtension"

      if (Fs.syncFile(specificPath).exists) {
        specificPath
      } else {
        defaultPath
      }
    }

    if (Fs.syncFile(basePath + "/" + f + ".ignore").exists) {
      ignore(s"Parse golden from json-ld for $name with form ${form.name}") {
        runCycle(f + ".ignore", f + ".ignore", target, target, Some(renderOptionsFor(form)))
      }
    } else {
      test(s"Parse golden from json-ld for $name with form ${form.name}") {
        runCycle(f, f, target, target, Some(renderOptionsFor(form)))
      }
    }

  }

  private def runCycle(source: String, target: Hint, renderOptions: Option[RenderOptions] = None): Future[Assertion] =
    runCycle(source, source, target, target, renderOptions)

  private def runCycle(
      source: String,
      golden: String,
      hint: Hint,
      target: Hint,
      renderOptions: Option[RenderOptions]
  ): Future[Assertion] = {
    cycle(source, golden, hint, target, eh = Some(DefaultErrorHandler()), renderOptions = renderOptions)
  }
}
