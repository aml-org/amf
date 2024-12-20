package amf.compiler

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Annotation
import amf.core.client.scala.traversal.iterator.AmfElementStrategy
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Raml10YamlHint
import org.mulesoft.common.client.lexical.PositionRange
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class RamlTypeExpressionAnnotationTest
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with Matchers
    with CompilerTestBuilder {

  val rootPath = "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/raml-type-expressions"

  private val aArray       = LexicalInformation(PositionRange((7, 10), (7, 15)))
  private val aUnionA      = LexicalInformation(PositionRange((8, 10), (8, 16)))
  private val aUnionB      = LexicalInformation(PositionRange((8, 19), (8, 21)))
  private val aUnion2A     = LexicalInformation(PositionRange((9, 11), (9, 17)))
  private val aUnion2B     = LexicalInformation(PositionRange((9, 26), (9, 28)))
  private val tripleUnionA = LexicalInformation(PositionRange((10, 16), (10, 21)))
  private val tripleUnionB = LexicalInformation(PositionRange((10, 24), (10, 29)))
  private val tripleUnionC = LexicalInformation(PositionRange((10, 32), (10, 34)))
  private val unionArrayA  = LexicalInformation(PositionRange((11, 15), (11, 20)))
  private val unionArrayB  = LexicalInformation(PositionRange((11, 23), (11, 28)))

  private val fBu: Future[BaseUnit] = build(s"$rootPath/many-expressions-01.raml", Raml10YamlHint)

  test("annotate range for simple array") {
    assertAnnotation(aArray)
  }

  test("annotate range for simple union") {
    assertAnnotation(aUnionA)
    assertAnnotation(aUnionB)
  }

  test("annotate range for union with array element") {
    assertAnnotation(aUnion2A)
    assertAnnotation(aUnion2B)
  }

  test("annotate range for union with three elements") {
    assertAnnotation(tripleUnionA)
    assertAnnotation(tripleUnionB)
    assertAnnotation(tripleUnionC)
  }

  test("annotate ranges in an array of unions") {
    assertAnnotation(unionArrayA)
    assertAnnotation(unionArrayB)
  }

  private def assertAnnotation(a: Annotation): Future[Assertion] =
    for {
      bu <- fBu
    } yield {
      assert(bu.containsAnnotation(a))
    }

  implicit class AnnotationsImpl(bu: BaseUnit) {

    private lazy val flattenAnnotations: List[Annotations] =
      bu.iterator(AmfElementStrategy).map(_.annotations).toList

    private lazy val flattenLexical: List[LexicalInformation] =
      flattenAnnotations.flatMap(_.find(classOf[LexicalInformation]))

    def containsAnnotation(a: Annotation): Boolean =
      flattenLexical.contains(a)
  }
}
