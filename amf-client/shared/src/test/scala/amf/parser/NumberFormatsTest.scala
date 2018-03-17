package amf.parser

import amf.common.Diff
import amf.common.Diff.makeString
import amf.core.client.GenerationOptions
import amf.core.model.document.Fragment
import amf.core.remote.Syntax.Yaml
import amf.core.remote.{Raml10, RamlYamlHint}
import amf.core.unsafe.{PlatformSecrets, TrunkPlatform}
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.plugins.domain.shapes.models.TypeDef.{DoubleType, FloatType, IntType, LongType}
import amf.plugins.domain.shapes.models.{ScalarShape, TypeDef}
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class NumberFormatsTest extends AsyncFunSuite with PlatformSecrets {
  implicit override def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val cases: Seq[FormatCases] = Seq(
    FormatCases("number", IntType, "int"),
    FormatCases("number", IntType, "int8"),
    FormatCases("number", IntType, "int16"),
    FormatCases("number", IntType, "int32"),
    FormatCases("number", LongType, "int64"),
    FormatCases("number", LongType, "long"),
    FormatCases("number", DoubleType, "double"),
    FormatCases("number", FloatType, "float"),
    FormatCases("number", FloatType, "")
  )

  cases.foreach { ex =>
    test(s"Test data type ${ex.literalType} format ${ex.format}") {
      for {
        validation <- Validation(platform).map(_.withEnabledValidation(false))
        unit       <- AMFCompiler("", TrunkPlatform(ex.api), RamlYamlHint, validation).build()
        dumped     <- Future { AMFDumper(unit, Raml10, Yaml, GenerationOptions()).dumpToString }
      } yield {
        unit match {
          case f: Fragment =>
            f.encodes match {
              case shape: ScalarShape =>
                shape.dataType.value() should be(ex.dataType)
                checkApi(dumped, ex.api)
              case other => fail(s"Cannot find a valid scalar shape encoded: $other")
            }
          case other => fail(s"Build not returned a valid dataType fragment: $other")
        }
      }
    }
  }

  private def checkApi(actual: String, expected: String): Assertion = {
    val diffs = Diff.ignoreAllSpace.diff(expected, actual)
    if (diffs.nonEmpty)
      fail(s"\nApis not equals, diffs: \n\n${makeString(diffs)}")
    else succeed
  }

  case class FormatCases(literalType: String, typeDef: TypeDef, format: String) {
    val dataType: String = XsdTypeDefMapping.xsd(typeDef)
    val api: String =
      s"""|#%RAML 1.0 DataType
        |type: $literalType""".stripMargin +
        (if (format.nonEmpty) s"\nformat: $format" else "")
  }
}
