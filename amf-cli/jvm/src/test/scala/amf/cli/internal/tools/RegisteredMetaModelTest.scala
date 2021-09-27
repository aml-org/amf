package amf.cli.internal.tools

import amf.aml.internal.entities.AMLEntities
import amf.apicontract.internal.entities.{APIEntities, FragmentEntities}
import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.{ModelDefaultBuilder, Obj}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.entities.ShapeEntities
import org.reflections.Reflections
import org.scalatest.{Assertion, FunSuite, Matchers}

class RegisteredMetaModelTest extends FunSuite with Matchers with PlatformSecrets {

  test("APIEntities contains all ApiContract domain entities") {
    check(new Reflections("amf.apicontract.internal.metamodel.domain"), APIEntities)
  }

  test("FragmentEntities contains all ApiContract document entities") {
    check(new Reflections("amf.apicontract.internal.metamodel.document"), FragmentEntities)
  }

  test("AMLEntities contains all AML entities") {
    check(new Reflections("amf.aml.internal"), AMLEntities)
  }

  test("ShapeEntities contains all shape entities") {
    check(new Reflections("amf.shapes.internal"), ShapeEntities)
  }

  private def check(reflection: Reflections, entities: Entities): Assertion = {
    val models = getMetaModels(reflection)
    val registeredModels = models
      .collect {
        case other if other.isInstanceOf[ModelDefaultBuilder] => other.asInstanceOf[ModelDefaultBuilder]
      }
      .filter { model =>
        !entities.contains(model)
      }
    withClue(s"There are ${registeredModels.size} models not registered: $registeredModels") {
      registeredModels shouldBe empty
    }
  }

  private def getMetaModels(reflection: Reflections): List[Obj] = {
    ObjLoader.metaObjects(reflection)
  }
}
