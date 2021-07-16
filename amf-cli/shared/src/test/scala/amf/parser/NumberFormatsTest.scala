package amf.parser

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.scala.model.document.Fragment
import amf.core.internal.remote.Raml10
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.spec.common.TypeDef._
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import amf.shapes.internal.spec.common.TypeDef
import org.mulesoft.common.test.Diff
import org.mulesoft.common.test.Diff.makeString
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
//    FormatCases("number", LongType, "long"),
    FormatCases("number", DoubleType, "double"),
    FormatCases("number", FloatType, "float"),
    FormatCases("number", NumberType, "")
  )

  cases.foreach { ex =>
    test(s"Test data type ${ex.literalType} format ${ex.format}") {
      val client = RAMLConfiguration.RAML10().baseUnitClient()
      for {
        unit   <- client.parseContent(ex.api)
        dumped <- Future.successful(client.render(unit.baseUnit, Raml10.mediaType))
      } yield {
        unit.baseUnit match {
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
