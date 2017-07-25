package amf.graph

import amf.builder.{DocumentBuilder, WebApiBuilder}
import org.scalatest.FunSuite

/**
  * [[GraphEmitter]] test
  */
class GraphEmitterTest extends FunSuite {

  test("Document encoding simple WebApi (using @context)") {
    val ast = GraphEmitter.emit(createDocument)
    println(ast)
  }

  test("Document encoding simple WebApi (expanded)") {
    val ast = GraphEmitter.emit(createDocument, expanded = true)
    println(ast)
  }

  private def createDocument = {
    val document = DocumentBuilder()
      .withEncodes(WebApiBuilder().withHost("api.example.com").withName("test").build)
      .build
    document
  }
}
