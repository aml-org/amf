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
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"b\"")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("Simple key value with white space parse test") {
    val input = "{\"a \": \"b\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a \""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"b\"")
      , (EndMap, "}"), (Eof, ""))
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
    val expected = List((StartMap,"{"),(WhiteSpace,escape("\n  ")),(StringToken,"\"a\""),
      (Colon,":"),(WhiteSpace," "),(Number,"1"),(Comma,","),
      (WhiteSpace,"\\n  "),(StringToken,"\"b\""),(Colon,":"),(WhiteSpace," "),
      (StartMap,"{"),(WhiteSpace,"\\n    "),(Link,"\"$ref\""),(Colon,":"),
      (WhiteSpace," "),(StringToken,"\"file://include1.json\""),(WhiteSpace,"\\n  "),
      (EndMap,"}"),(Comma,","),(WhiteSpace,"\\n  "),(StringToken,"\"c\""),
      (Colon,":"),(WhiteSpace," "),(StartSequence,"["),(WhiteSpace,"\\n    "),
      (Number,"2"),(WhiteSpace,"\\n  "),(EndSequence,"]"),(WhiteSpace,"\\n"),
      (EndMap,"}"),(Eof,""))
    assert(actual,expected)
  }
}
