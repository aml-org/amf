package amf.compiler

import amf.core.client.common.position.Range
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Annotation
import amf.core.client.scala.traversal.iterator.AmfElementStrategy
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Raml10YamlHint
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class RamlTypeExpressionAnnotationTest extends AsyncFlatSpec with Matchers with CompilerTestBuilder {
  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  val rootPath = "file://amf-cli/shared/src/test/resources/nodes-annotations-examples/raml-type-expressions"

  behavior of "RAML type expression annotations"

  private val aArray       = LexicalInformation(Range((7, 10), (7, 15)))
  private val aUnionA      = LexicalInformation(Range((8, 10), (8, 16)))
  private val aUnionB      = LexicalInformation(Range((8, 19), (8, 21)))
  private val aUnion2A     = LexicalInformation(Range((9, 11), (9, 17)))
  private val aUnion2B     = LexicalInformation(Range((9, 26), (9, 28)))
  private val tripleUnionA = LexicalInformation(Range((10, 16), (10, 21)))
  private val tripleUnionB = LexicalInformation(Range((10, 24), (10, 29)))
  private val tripleUnionC = LexicalInformation(Range((10, 32), (10, 34)))
  private val unionArrayA  = LexicalInformation(Range((11, 15), (11, 20)))
  private val unionArrayB  = LexicalInformation(Range((11, 23), (11, 28)))

  private val fBu: Future[BaseUnit] = build(s"$rootPath/many-expressions-01.raml", Raml10YamlHint)

  it should "annotate range for simple array" in {
    test(aArray)
  }

  it should "annotate range for simple union" in {
    test(aUnionA)
    test(aUnionB)
  }

  it should "annotate range for union with array element" in {
    test(aUnion2A)
    test(aUnion2B)
  }

  it should "annotate range for union with three elements" in {
    test(tripleUnionA)
    test(tripleUnionB)
    test(tripleUnionC)
  }

  it should "annotate ranges in an array of unions" in {
    test(unionArrayA)
    test(unionArrayB)
  }

  private def test(a: Annotation): Future[Assertion] =
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
