package amf.builder

import amf.metadata.domain.WebApiModel._
import amf.model.Annotation.LexicalInformation
import amf.parser.Range
import amf.unsafe.PlatformSecrets
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by pedro.colunga on 7/12/17.
  */
class BuilderTest extends FunSuite with PlatformSecrets {

  test("Annotations") {

    val api = builders.webApi

    api.set(Name, "Altoids", List(LexicalInformation(Range((1, 1), (1, 8)))))

    val result = api.build

    val annotations = result.fields.getValue(Name).annotations

    annotations should have size 1

    annotations.mkString("[", ",", "]") should be("[LexicalInformation([(1,1)-(1,8)])]")
  }
}
