package amf.compiler

import amf.core.annotations.{SourceAST, SourceNode}
import amf.core.model.domain.{AmfArray, AmfObject}
import amf.core.remote.{OasJsonHint, RamlYamlHint}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext

class SourceNodeAnnotationTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test full raml") {
    build("file://amf-client/shared/src/test/resources/upanddown/full-example.raml", RamlYamlHint) map {
      checkAnnotation
    }
  }

  test("test full oas") {
    build("file://amf-client/shared/src/test/resources/upanddown/full-example.json", OasJsonHint) map {
      checkAnnotation
    }
  }

  test("test full raml with type xml tag") {
    build("file://amf-client/shared/src/test/resources/org/raml/api/v10/xmlbodyinlinewithfacet/input.raml",
          RamlYamlHint) map { checkAnnotation }
  }

  private def checkAnnotation(obj: AmfObject): Assertion = {
    obj.fields.foreach {
      case (field, value) =>
        value.value match {
          case amfObject: AmfObject =>
            if (amfObject.annotations.contains(classOf[SourceAST]))
              assert(amfObject.annotations.contains(classOf[SourceNode]))
            checkAnnotation(amfObject)
          case array: AmfArray if array.values.head.isInstanceOf[AmfObject] =>
            array.values.asInstanceOf[Seq[AmfObject]].foreach { checkAnnotation }
          case _ =>
        }
    }
    succeed
  }
}
