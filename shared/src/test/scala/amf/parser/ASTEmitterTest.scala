package amf.parser

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.yaml.model.YTag

/**
  * [[ASTEmitter]] test
  */
class ASTEmitterTest extends FunSuite {

  test("Simple AST") {
    val emitter = ASTEmitter()

    val document = emitter.document { () =>
      emitter.mapping { () =>
        emitter.entry { () =>
          emitter.scalar("a", YTag.Str)
          emitter.scalar(1, YTag.Int)
        }
        emitter.entry { () =>
          emitter.scalar("b", YTag.Str)
          emitter.sequence { () =>
            emitter.scalar(1, YTag.Int)
            emitter.scalar(2, YTag.Int)
          }
        }
      }
    }

    document.toString() should be("""|Document:
         |!!map   {
         |    !!str "a": !!int "1"
         |    !!str "b": !!seq [
         |!!int   "1"
         |!!int   "2"
         |]
         |  }
         |""".stripMargin)
  }
}
