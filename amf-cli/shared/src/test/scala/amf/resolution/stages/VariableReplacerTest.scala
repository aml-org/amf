package amf.resolution.stages

import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.model.domain.templates.Variable
import amf.core.internal.transform.VariableReplacer
import org.scalatest.Inspectors
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer

/** */
class VariableReplacerTest extends AnyFunSuite with Matchers with Inspectors {

  case class ReplacerExamples(name: String, transformation: String, base: String, result: String)

  val examples = List(
    ReplacerExamples("singularize", "singularize", "users", "user"),
    ReplacerExamples("singularize ending with 'ies'", "singularize", "entities", "entity"),
    ReplacerExamples("pluralize", "pluralize", "user", "users"),
    ReplacerExamples("pluralize ending with 'y'", "pluralize", "company", "companies"),
    ReplacerExamples("uppercase", "uppercase", "userId", "USERID"),
    ReplacerExamples("lowercase", "lowercase", "userId", "userid"),
    ReplacerExamples("lowercamelcase", "lowercamelcase", "UserId", "userId"),
    ReplacerExamples("uppercamelcase", "uppercamelcase", "userId", "UserId"),
    ReplacerExamples("uppercamelcase hyphen", "uppercamelcase", "user-id", "UserId"),
    ReplacerExamples("uppercamelcase underscore", "uppercamelcase", "user_id", "UserId"),
    ReplacerExamples("uppercamelcase space", "uppercamelcase", "user id", "UserId"),
    ReplacerExamples("uppercamelcase multiple hyphen", "uppercamelcase", "user---id", "UserId"),
    ReplacerExamples("uppercamelcase multiple underscore", "uppercamelcase", "user___id", "UserId"),
    ReplacerExamples("uppercamelcase multiple combined", "uppercamelcase", "user _-_ id", "UserId"),
    ReplacerExamples("lowerunderscorecase", "lowerunderscorecase", "userId", "user_id"),
    ReplacerExamples("upperunderscorecase", "upperunderscorecase", "userId", "USER_ID"),
    ReplacerExamples("lowerhyphencase", "lowerhyphencase", "userId", "user-id"),
    ReplacerExamples("upperhyphencase", "upperhyphencase", "userId", "USER-ID")
  )

  examples.foreach { example =>
    test(s"Test transformation : ${example.name} of Raml spec example") {
      VariableReplacer.variableTransformation((e: String) => fail(e))(example.base, example.transformation) should be(
        example.result
      )
    }
  }

  test("Replace Variables") {

    case class Replacement(expression: String, variable: (String, String), expected: String)

    val replacements = Set(
      Replacement(
        "<<resourcePathName|!singularize|!uppercamelcase>>",
        "resourcePathName" -> "preferredCustomers",
        "PreferredCustomer"
      ),
      Replacement(
        "<<resourcePathName | !singularize | !uppercamelcase>>",
        "resourcePathName" -> "preferredCustomers",
        "PreferredCustomer"
      ),
      Replacement("<<resourcePathName|!singularize>>", "resourcePathName" -> "preferredCustomers", "preferredCustomer"),
      Replacement("<<resourcePathName| !singularize>>", "resourcePathName" -> "preferredCustomers", "preferredCustomer")
    )

    val errors = ListBuffer[String]()
    forAll(replacements) { replacement =>
      val node      = ScalarNode(replacement.expression, None)
      val variables = Set(Variable(replacement.variable._1, ScalarNode(replacement.variable._2, None)))
      val result    = VariableReplacer.replaceNodeVariables(node, variables, (message: String) => errors ++ message)
      errors.isEmpty should be(true)
      result.asInstanceOf[ScalarNode].value.value() should be(replacement.expected)
    }
  }
}
