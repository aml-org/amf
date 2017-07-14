package amf.builder

import amf.metadata.domain.APIDocumentationModel._
import amf.domain.Annotation.LexicalInformation
import amf.parser.Range
import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  *
  */
class BuilderTest extends FunSuite with PlatformSecrets {

  test("Annotations") {

    val api = APIDocumentationBuilder()

    api.set(Name, "Altoids", List(LexicalInformation(Range((1, 1), (1, 8)))))

    val result = api.build

    val annotations = result.fields.getValue(Name).annotations

    annotations should have size 1

    annotations.mkString("[", ",", "]") should be("[LexicalInformation([(1,1)-(1,8)])]")
  }
}
