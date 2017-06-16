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

  test("Simple key numeric value parse test") {
    val input = "{\"a\": 2}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "), (Number, "2")
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

  test("object with string sequence parse test") {
    val input = "{\"a\": [\"b\",\"c\",\"d\",\"e\"]}"
     val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "),(StartSequence,"["), (StringToken, "\"b\""),
        (Comma,",") ,(StringToken, "\"c\""),(Comma,",") ,(StringToken, "\"d\""),(Comma,",") ,(StringToken, "\"e\"")
      ,(EndSequence,"]"), (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("object with number sequence parse test") {
    val input = "{\"a\": [1,2,3,4]}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "),(StartSequence,"["), (Number, "1"),
      (Comma,",") ,(Number, "2"),(Comma,",") ,(Number, "3"),(Comma,",") ,(Number, "4")
      ,(EndSequence,"]"), (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("object with object sequence parse test") {
    val input = "{\"a\": [{\"b\":1},{\"c\":2},{\"d\":3}]}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "),(StartSequence,"["),
        (StartMap,"{"), (StringToken,"\"b\""),(Colon,":"),(Number, "1"),(EndMap,"}"),(Comma,","),
        (StartMap,"{"), (StringToken,"\"c\""),(Colon,":"),(Number, "2"),(EndMap,"}"),(Comma,","),
        (StartMap,"{"),(StringToken,"\"d\""),(Colon,":"),(Number, "3"),(EndMap,"}"),
      (EndSequence,"]"), (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("sequence of sequence parse test") {
    val input = "{\"a\": [[\"b\",\"c\",\"d\"],[\"e\",\"f\",\"g\"]]}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "),
        (StartSequence,"["),
          (StartSequence,"["),
            (StringToken,"\"b\""),(Comma,","),(StringToken,"\"c\""),(Comma,","),(StringToken,"\"d\""),
          (EndSequence,"]"),
          (Comma,","),
          (StartSequence,"["),
            (StringToken,"\"e\""),(Comma,","),(StringToken,"\"f\""),(Comma,","),(StringToken,"\"g\""),
          (EndSequence,"]"),
        (EndSequence,"]"),
      (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("only sequence parse test") {
    val input = "[\"b\",\"c\",\"d\"]"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List(
      (StartSequence,"["),
      (StringToken,"\"b\""),(Comma,","),(StringToken,"\"c\""),(Comma,","),(StringToken,"\"d\""),
      (EndSequence,"]"),
      (Eof, ""))

    assert(actual,expected)
  }

  test("mixed types sequences parse test") {
    val input = "[[\"b\",\"c\",\"d\"],{\"key\":\"value\"}]"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List(
      (StartSequence,"["),
        (StartSequence,"["),
          (StringToken,"\"b\""),(Comma,","),(StringToken,"\"c\""),(Comma,","),(StringToken,"\"d\""),
        (EndSequence,"]"),
        (Comma,","),
          (StartMap,"{"),
            (StringToken,"\"key\""),
            (Colon,":"),
            (StringToken,"\"value\""),
          (EndMap,"}"),
      (EndSequence,"]"),
      (Eof, ""))

    assert(actual,expected)
  }

  test("mixed types sequences with ws parse test") {
    val input = " [\n[\" b\" , \"c \" , \"d\" ] , { \"key \" : \" value\" } ]"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List(
      (WhiteSpace," "),
      (StartSequence,"["),(WhiteSpace,"\\n"),
        (StartSequence,"["),
          (StringToken,"\" b\""),(WhiteSpace," "),(Comma,","),(WhiteSpace," "),(StringToken,"\"c \""),(WhiteSpace," "),(Comma,","),(WhiteSpace," "),(StringToken,"\"d\""),(WhiteSpace," "),
        (EndSequence,"]"),(WhiteSpace," "),
        (Comma,","),(WhiteSpace," "),
        (StartMap,"{"),(WhiteSpace," "),
          (StringToken,"\"key \""),(WhiteSpace," "),
          (Colon,":"),(WhiteSpace," "),
          (StringToken,"\" value\""),(WhiteSpace," "),
        (EndMap,"}"),(WhiteSpace," "),
      (EndSequence,"]"),
      (Eof, ""))

    assert(actual,expected)
  }

  test("object of objects parse test") {
    val input = """{
                   |  "obj1L1":{
                   |    "obj1L2": {"a": "1", "b": 2},
                   |    "obj2L2":{"z": "99", "y": "98"}
                   |  },
                   |  "obj2L1":{
                   |    "obj1L2": {"l": "50"}
                   |  }
                   |}""".stripMargin
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List(
      (StartMap, "{"), (WhiteSpace, "\\n  "),
  /*(WhiteSpace,"\\t"),*/
      (StringToken,"\"obj1L1\""),(Colon,":"),
        (StartMap,"{"),(WhiteSpace,"\\n    "),
          (StringToken,"\"obj1L2\""),(Colon,":"),(WhiteSpace," "),
            (StartMap,"{"),
              (StringToken,"\"a\""),(Colon,":"),(WhiteSpace," "),(StringToken,"\"1\""),
              (Comma,","),(WhiteSpace," "),
   /*<-->*/   (StringToken,"\"b\""),(Colon,":"),(WhiteSpace," "),(Number,"2"),
            (EndMap,"}"),(Comma,","),(WhiteSpace,"\\n    "),
          (StringToken,"\"obj2L2\""),(Colon,":"),
            (StartMap,"{"),
              (StringToken,"\"z\""),(Colon,":"),(WhiteSpace," "),(StringToken,"\"99\""),
              (Comma,","),(WhiteSpace," "),
   /*<-->*/   (StringToken,"\"y\""),(Colon,":"),(WhiteSpace," "),(StringToken,"\"98\""),
            (EndMap,"}"),(WhiteSpace,"\\n  "),
        (EndMap,"}"),
        (Comma,","),(WhiteSpace,"\\n  "),
        (StringToken,"\"obj2L1\""),(Colon,":"),
        (StartMap,"{"),(WhiteSpace,"\\n    "),
          (StringToken,"\"obj1L2\""),(Colon,":"),(WhiteSpace," "),
            (StartMap,"{"),
              (StringToken,"\"l\""),(Colon,":"),(WhiteSpace," "),(StringToken,"\"50\""),
            (EndMap,"}"),(WhiteSpace,"\\n  "),
        (EndMap,"}"),(WhiteSpace,"\\n"),
      (EndMap,"}"),
      (Eof,""))

    assert(actual,expected)
  }

  test("Null value parse test") {
    val input = "{\"a\": null}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "), (Null, "null")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("Boolean value parse test") {
    val input = "{\"a\": true}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "), (True, "true")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("String with boolean value parse test") {
    val input = "{\"a\": \"true\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (StringToken, "\"a\""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"true\"")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("Sequence of keywords parse test") {
    val input = "[null,true,false]"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartSequence, "["),
        (Null, "null"), (Comma, ","),(True, "true"),(Comma,",") ,(False, "false")
      , (EndSequence, "]"), (Eof, ""))

    assert(actual,expected)
  }

  //JSON-LD
  test("Simple json-ld parse test with @context") {
    val input = "{\"@context\": \"http://schema.org/\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"@context\""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"http://schema.org/\"")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("Simple json-ld parse test with @type") {
    val input = "{\"@type\": \"Car\"}"
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"), (JsonToken.StringToken, "\"@type\""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"Car\"")
      , (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

  test("Simple json-ld parse test with more than one annotated words") {
    val input =
      """{
        |  "@context": "http://schema.org/",
        |  "@type": "Person"
        |}""".stripMargin
    val actual: List[(Token, String)] = JsonLexer(input).lex()
    println(actual.toString)
    val expected = List((StartMap, "{"),(WhiteSpace,"\\n  "),
      (JsonToken.StringToken, "\"@context\""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"http://schema.org/\""),(Comma,","),(WhiteSpace,"\\n  "),
      (JsonToken.StringToken, "\"@type\""),
      (Colon, ":"),(WhiteSpace, " "), (StringToken, "\"Person\""),(WhiteSpace,"\\n"),
      (EndMap, "}"), (Eof, ""))

    assert(actual,expected)
  }

}
