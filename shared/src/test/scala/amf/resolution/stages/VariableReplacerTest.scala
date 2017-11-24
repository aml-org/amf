package amf.resolution.stages

import amf.plugins.domain.webapi.resolution.stages.VariableReplacer
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  *
  */
class VariableReplacerTest extends FunSuite {

  case class ReplacerExamples(transformation: String, base: String, result: String)

  val examples = List(
    ReplacerExamples("singularize", "users", "user"),
    ReplacerExamples("pluralize", "user", "users"),
    ReplacerExamples("uppercase", "userId", "USERID"),
    ReplacerExamples("lowercase", "userId", "userid"),
    ReplacerExamples("lowercamelcase", "UserId", "userId"),
    ReplacerExamples("uppercamelcase", "userId", "UserId"),
    ReplacerExamples("lowerunderscorecase", "userId", "user_id"),
    ReplacerExamples("upperunderscorecase", "userId", "USER_ID"),
    ReplacerExamples("lowerhyphencase", "userId", "user-id"),
    ReplacerExamples("upperhyphencase", "userId", "USER-ID")
  )

  examples.foreach { example =>
    test(s"Test transformation : ${example.transformation} of Raml spec example") {
      VariableReplacer.variableTransformation(example.base, example.transformation) should be(example.result)
    }
  }
}
