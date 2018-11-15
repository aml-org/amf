package amf.resolution.stages

import amf.core.model.domain.ScalarNode
import amf.core.model.domain.templates.Variable
import amf.core.resolution.VariableReplacer
import org.scalatest.{FunSuite, Inspectors, Matchers}

import scala.collection.mutable.ListBuffer

/**
  *
  */
class VariableReplacerTest extends FunSuite with Matchers with Inspectors {

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
      VariableReplacer.variableTransformation((e: String) => fail(e))(example.base, example.transformation) should be(
        example.result)
    }
  }

  test("Replace Variables") {

    case class Replacement(expression: String, variable: (String, String), expected: String)

    val replacements = Set(
      Replacement("<<resourcePathName|!singularize|!uppercamelcase>>",
                  "resourcePathName" -> "preferredCustomers",
                  "PreferredCustomer"),
      Replacement("<<resourcePathName | !singularize | !uppercamelcase>>",
                  "resourcePathName" -> "preferredCustomers",
                  "PreferredCustomer"),
      Replacement("<<resourcePathName|!singularize>>",
                  "resourcePathName" -> "preferredCustomers",
                  "preferredCustomer"),
      Replacement("<<resourcePathName| !singularize>>",
                  "resourcePathName" -> "preferredCustomers",
                  "preferredCustomer")
    )

    val errors = ListBuffer[String]()
    forAll(replacements) { replacement =>
      val node      = ScalarNode(replacement.expression, None)
      val variables = Set(Variable(replacement.variable._1, ScalarNode(replacement.variable._2, None)))
      val result    = VariableReplacer.replaceNodeVariables(node, variables, (message: String) => errors ++ message)
      errors.isEmpty should be(true)
      result.asInstanceOf[ScalarNode].value should be(replacement.expected)
    }
  }
}
