package amf.emit

import amf.parser.ASTEmitter
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.yaml.model.YType

/**
  * [[ASTEmitter]] test
  */
class ASTEmitterTest extends FunSuite {

  test("Simple AST") {
    val emitter = ASTEmitter()

    val document = emitter.document { () =>
      emitter.mapping { () =>
        emitter.entry { () =>
          emitter.scalar("a", YType.Str)
          emitter.scalar(1, YType.Int)
        }
        emitter.entry { () =>
          emitter.scalar("b", YType.Str)
          emitter.sequence { () =>
            emitter.scalar(1, YType.Int)
            emitter.scalar(2, YType.Int)
          }
        }
      }
    }

    document.toString() shouldBe "Document: {\"a\": \"1\", \"b\": [\"1\", \"2\"]}"
  }
}
