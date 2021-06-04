package amf.maker

import amf.plugins.document.apicontract.parser.RamlFragmentHeader.Raml10DataType
import amf.plugins.document.apicontract.parser.RamlHeader
import amf.plugins.document.apicontract.parser.RamlHeader.{Raml08, Raml10, Raml10Library}
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class RamlHeaderPatternTest extends FunSuite {

  case class HeaderCase(useCase: String, text: String, expected: Option[RamlHeader])

  val fixture = Seq(
    HeaderCase("Normal api", "%RAML 1.0", Some(Raml10)),
    HeaderCase("One space header api", " %RAML 1.0 ", Some(Raml10)),
    HeaderCase("More than one space header api", "   %RAML   1.0   ", Some(Raml10)),
    HeaderCase("No spaces header api", "%RAML1.0", Some(Raml10)),
    HeaderCase("Normal api08", "%RAML 0.8", Some(Raml08)),
    HeaderCase("One space header api08", " %RAML 0.8 ", Some(Raml08)),
    HeaderCase("More than one space header api08", "   %RAML   0.8   ", Some(Raml08)),
    HeaderCase("No spaces header api08", "%RAML0.8", Some(Raml08)),
    HeaderCase("Normal library", "%RAML 1.0 Library", Some(Raml10Library)),
    HeaderCase("One space header library", " %RAML 1.0 Library ", Some(Raml10Library)),
    HeaderCase("More than one space header library", "   %RAML   1.0   Library   ", Some(Raml10Library)),
    HeaderCase("No spaces header library", "%RAML1.0Library", Some(Raml10Library)),
    HeaderCase("Normal fragment", "%RAML 1.0 DataType", Some(Raml10DataType)),
    HeaderCase("One space header fragment", " %RAML 1.0 DataType ", Some(Raml10DataType)),
    HeaderCase("More than one space header fragment", "   %RAML   1.0   DataType   ", Some(Raml10DataType)),
    HeaderCase("No spaces header fragment", "%RAML1.0DataType", Some(Raml10DataType)),
    HeaderCase("Invalid header", "RAMLaa1.0aaDataType", None)
  )

  fixture.foreach { headerCase =>
    test(s"Raml header pattern test ${headerCase.useCase}") {
      RamlHeader.fromText(headerCase.text) should be(headerCase.expected)
    }
  }

}
