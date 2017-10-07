package amf.resolution

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Yaml
import amf.remote.{Raml, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class ResolutionTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/resolution/"

  val examples = Seq(
    "union1",
    "union2"
    // "inheritance1"
  )

  examples.foreach { example =>
    test(s"Resolve data types: $example") {
      val expected   = platform.resolve(basePath + s"${example}_canonical.raml", None).map(_.stream.toString)
      AMFCompiler(basePath + s"${example}.raml",
        platform,
        RamlYamlHint,
        None,
        None,
        platform.dialectsRegistry)
        .build().map { model =>
        new ShapeNormalizationStage(ProfileNames.RAML).resolve(model, null)
      }.flatMap({ unit =>
        AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString
      })
        .zip(expected)
        .map(checkDiff)
    }
  }

}
