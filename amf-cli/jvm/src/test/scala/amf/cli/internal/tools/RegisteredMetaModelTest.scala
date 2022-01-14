package amf.cli.internal.tools

import amf.aml.internal.entities.AMLEntities
import amf.apicontract.internal.entities.{APIEntities, FragmentEntities}
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.core.internal.entities.Entities
import amf.core.internal.metamodel.{ModelDefaultBuilder, Obj}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.entities.ShapeEntities
import org.reflections.Reflections
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class RegisteredMetaModelTest extends AnyFunSuite with Matchers with PlatformSecrets {

  test("APIEntities contains all ApiContract domain entities") {
    check(new Reflections("amf.apicontract.internal.metamodel.domain"), APIEntities, blocked = List(BaseApiModel))
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

  private def check(reflection: Reflections, entities: Entities, blocked: Seq[ModelDefaultBuilder] = Nil): Assertion = {
    val models         = getMetaModels(reflection)
    val actualBuilders = models.collect { case b: ModelDefaultBuilder => b }
    val missingEntities = actualBuilders
      .filter { model =>
        !blocked.contains(model) && !entities.contains(model) // must be registered and not present in entities
      }
    val registeredButBlocked = blocked.filter { entities.contains }

    withClue(s"There are ${missingEntities.size} models not registered: $missingEntities") {
      missingEntities shouldBe empty
    }
    withClue(s"There are ${registeredButBlocked.size} models that should not be registered: $registeredButBlocked") {
      registeredButBlocked shouldBe empty
    }
  }

  private def getMetaModels(reflection: Reflections): List[Obj] = {
    ObjLoader.metaObjects(reflection)
  }
}
