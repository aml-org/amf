package amf.cycle
import amf.client.parse.DefaultParserErrorHandler
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.Syntax.Syntax
import amf.core.remote.{AmfJsonHint, Hint}
import amf.io.BuildCycleTests
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
    if (d.name.endsWith(".ignore")) {
      s"Source: ${d.name}" ignore { Future.successful(succeed) }
    } else {
      s"Source: ${d.name}" - {
        simpleCycle(d.name)

        goldenCycle(d.name, d.name + "/dumped" + fileExtension)

        s"Steps for jsonld serialization ${d.name}" - {
          s"Generate jsonld for ${d.name}" in {
            val t = d.name + "/api" + fileExtension + ".jsonld" + (if (Fs.syncFile(
                                                                           basePath + d.name + "/api" + fileExtension + ".jsonld" + ".ignore")
                                                                         .exists) ".ignore"
                                                                   else "")
            runCycle(d.name + "/api" + fileExtension, t, origin, AmfJsonHint, None)
          }
          amfToSpec(d.name,
                    d.name + "/api" + fileExtension + ".jsonld",
                    d.name + "/api" + fileExtension + ".jsonld" + fileExtension)
          specToAmfForAmf(d.name, d.name + "/api" + fileExtension + ".jsonld" + fileExtension)
        }
      }
    }
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

  private def amfToSpec(name: String, o: String, t: String): Unit = {
    val tar = if (Fs.syncFile(basePath + "/" + t + ".ignore").exists) t + ".ignore" else t

    if (Fs.syncFile(basePath + "/" + o + ".ignore").exists) {

      s"Generate golden from jsonld for $name" ignore {
        runCycle(o + ".ignore", tar, AmfJsonHint, target, syntax = Some(target.syntax))
      }
    } else {
      s"Generate golden from jsonld for $name" in {
        runCycle(o, tar, AmfJsonHint, target, syntax = Some(target.syntax))
      }
    }
  }

  private def specToAmfForAmf(name: String, f: String): Unit = {
    if (Fs.syncFile(basePath + "/" + f + ".ignore").exists) {
      s"Parse golden from jsonld for $name" ignore {
        runCycle(f + ".ignore", f + ".ignore", target, target, syntax = Some(target.syntax))
      }
    } else {
      s"Parse golden from jsonld for $name" in {
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
