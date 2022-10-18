package amf.shapes.client.jsonldschema.spec.builder

import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Type
import amf.shapes.internal.spec.jsonldschema.parser.builder.{AnonObj, ArrayTypeComputation}
import amf.shapes.internal.spec.jsonldschema.parser.builder.ArrayTypeComputation.computeType
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class ArrayTypeComputationTest extends AnyFunSuite with Matchers {

  private val base = Namespace("http://somebase#")
  private val iri  = (fragment: String) => ValueType(base, fragment)

  private val any       = Type.Any
  private val int       = Type.Int
  private val string    = Type.Str
  private val boolean   = Type.Bool
  private val boolArray = Type.Array(boolean)
  private val intArray  = Type.Array(int)
  private val anyArray  = Type.Array(any)
  private val abcObj    = AnonObj(iri("a"), iri("b"), iri("c"))
  private val aObj      = AnonObj(iri("a"))
  private val bcObj     = AnonObj(iri("b"), iri("c"))
  private val dObj      = AnonObj(iri("d"))

  private val combinations = Table(
    ("left", "right", "result"),
    (int, string, any),
    (int, int, int),
    (boolean, int, any),
    (boolArray, boolArray, boolArray),
    (boolArray, intArray, anyArray),
    (abcObj, aObj, aObj),
    (bcObj, int, any),
    (bcObj, dObj, any)
  )

  forAll(combinations) { (left: Type, right: Type, result: Type) =>
    computeType(left, right) should equal(result)
  }
}
