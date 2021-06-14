package amf.spec

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.internal.render.SpecOrdering.Lexical
import amf.core.internal.render.emitters.Emitter
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by pedro.colunga on 8/23/17.
  */
class SpecOrderingTest extends FunSuite {

  test("Test lexical spec ordering - with lexical") {
    val a = PosEmitter("a", Position(1, 0))
    val b = PosEmitter("b", Position(2, 0))
    val c = PosEmitter("c", Position(3, 0))

    val values = Seq[Emitter](a, c, b)

    Lexical.sorted(values) should contain theSameElementsInOrderAs List(a, b, c)
  }

  test("Test lexical spec ordering - without lexical") {
    val a = PosEmitter("a", ZERO)
    val b = PosEmitter("b", ZERO)
    val c = PosEmitter("c", ZERO)

    val values = Seq[Emitter](a, c, b)

    Lexical.sorted(values) should contain theSameElementsInOrderAs List(a, c, b)
  }

  test("Test lexical spec ordering - mixed lexical") {
    val a = PosEmitter("a", Position(1, 0))
    val b = PosEmitter("b", Position.ZERO)
    val c = PosEmitter("c", Position(2, 0))
    val d = PosEmitter("d", Position(3, 0))

    val values = Seq(a, b, c, d)

    Lexical.sorted(values) should contain theSameElementsInOrderAs List(a, c, d, b)
  }

  case class PosEmitter(id: String, position: Position) extends Emitter {
    override def toString: String = id
  }
}
