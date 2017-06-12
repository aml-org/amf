package amf.lexer

import amf.json.JsonLexer
import amf.yaml.YamlLexer
import org.scalatest.FunSuite
/**
  * Created by pedro.colunga on 5/12/17.
  */

class LexerTest extends FunSuite{

  test("Simple key value parse test"){
    val input=
      """
        |a: 1
      """.stripMargin
    val actual: String = YamlLexer(input).lex()
    assert(actual.equals("WS:\"\\n\", {:\": \", Str:\"a\", Str:\"1\", WS:\"\\n\", WS:\"      \", }:\"\""))
  }
  test("Test complete yaml example"){
      val input =
          """
            | # Yaml Example
            | a: 1
            | b: !include file://include1.yaml
            | c:
            |  - 2
            | --- # Document Start
            | ... # Document End
          """.stripMargin
      val actual: String = YamlLexer(input).lex()
    val expected: String = "WS:\"\\n\", WS:\" \", Comment:\"# Yaml Example\\n\", WS:\" \", {:\": \", Str:\"a\", Str:\"1\", WS:\"\\n\", WS:\" \", ,:\": \", Str:\"b\", Link:\"!include \", Str:\"file://include1.yaml\", WS:\"\\n\", WS:\" \", ,:\":\\n\", Str:\"c\", WS:\"  \", [:\"- \", Str:\"2\", WS:\"\\n\", WS:\" \", ]:\"---\", WS:\" \", Comment:\"# Document Start\\n\", WS:\" \", }:\"...\", WS:\" \", Comment:\"# Document End\\n\", WS:\"          \""
      assert(actual.equals(expected))
  }

  test("Test complete json example"){
    val input =
        """{
          |  "a": 1,
          |  "b": {
          |    "$ref": "file://include1.json"
          |  },
          |  "c": [
          |    2
          |  ]
          |}""".stripMargin
    val actual: String = JsonLexer(input).lex()
    val expected : String = "{:\"{\", WS:\"\\n  \", Str:\"\"a\"\", ::\":\", WS:\" \", Number:\"1\", ,:\",\", WS:\"\\n  \", Str:\"\"b\"\", ::\":\", WS:\" \", {:\"{\", WS:\"\\n    \", Link:\"\"$ref\"\", ::\":\", WS:\" \", Str:\"\"file://include1.json\"\", WS:\"\\n  \", }:\"}\", ,:\",\", WS:\"\\n  \", Str:\"\"c\"\", ::\":\", WS:\" \", [:\"[\", WS:\"\\n    \", Number:\"2\", WS:\"\\n  \", ]:\"]\", WS:\"\\n\", }:\"}\""
    assert(actual.equals(expected))
  }
}