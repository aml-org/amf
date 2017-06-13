package amf.lexer

import amf.common.ListAssertions
import amf.json.{JsonLexer, JsonToken}
import org.scalatest.FunSuite
import amf.common.Strings.escape
import amf.json.JsonToken._

/**
  * Created by hernan.najles on 6/12/17.
  */
class JsonLexerTest extends FunSuite with ListAssertions{

  test("Simple key value parse test") {
    val input = "{\"a\": \"b\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"a\""),
      (JsonToken.Colon, ":"),(JsonToken.WhiteSpace, " "), (JsonToken.StringToken, "\"b\"")
      , (JsonToken.EndMap, "}"), (JsonToken.Eof, ""))

    assert(actual,expected)
  }

  test("Simple key value with white space parse test") {
    val input = "{\"a \": \"b\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"a \""),
      (JsonToken.Colon, ":"),(JsonToken.WhiteSpace, " "), (JsonToken.StringToken, "\"b\"")
      , (JsonToken.EndMap, "}"), (JsonToken.Eof, ""))
    assert(actual,expected)
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
    val actual: List[(Token,String)] = JsonLexer(input).lex()
    val expected = List((StartMap,"{"),(JsonToken.WhiteSpace,escape("\n  ")),(JsonToken.StringToken,"\"a\""),
      (JsonToken.Colon,":"),(JsonToken.WhiteSpace," "),(JsonToken.Number,"1"),(JsonToken.Comma,","),
      (JsonToken.WhiteSpace,"\\n  "),(JsonToken.StringToken,"\"b\""),(JsonToken.Colon,":"),(JsonToken.WhiteSpace," "),
      (StartMap,"{"),(JsonToken.WhiteSpace,"\\n    "),(JsonToken.Link,"\"$ref\""),(JsonToken.Colon,":"),
      (JsonToken.WhiteSpace," "),(JsonToken.StringToken,"\"file://include1.json\""),(JsonToken.WhiteSpace,"\\n  "),
      (JsonToken.EndMap,"}"),(JsonToken.Comma,","),(JsonToken.WhiteSpace,"\\n  "),(JsonToken.StringToken,"\"c\""),
      (JsonToken.Colon,":"),(JsonToken.WhiteSpace," "),(JsonToken.StartSequence,"["),(JsonToken.WhiteSpace,"\\n    "),
      (JsonToken.Number,"2"),(JsonToken.WhiteSpace,"\\n  "),(JsonToken.EndSequence,"]"),(JsonToken.WhiteSpace,"\\n"),
      (JsonToken.EndMap,"}"),(JsonToken.Eof,""))
    assert(actual,expected)
  }
}
