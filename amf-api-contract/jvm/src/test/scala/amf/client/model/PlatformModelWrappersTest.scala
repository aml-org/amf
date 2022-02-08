package amf.client.model

import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.unsafe.PlatformSecrets
import org.reflections.Reflections
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.JavaConverters.asScalaSetConverter


class PlatformModelWrappersTest extends AnyFunSuite with Matchers with PlatformSecrets {

  test("All models have platform wrappers registered") {
    WebAPIConfiguration.WebAPI() // registers all wrappers, remove when APIMF-3000 is done
    val reflections = List(
      new Reflections("amf.apicontract.client.scala.model"),
      new Reflections("amf.shapes.client.scala.model"),
      new Reflections("amf.aml.client.scala.model"),
      new Reflections("amf.core.client.scala.model"),
    )
    val instances = obtainModelInstances(reflections)
    instances.foreach(callPlatformWrap)
    succeed
  }

  private def obtainModelInstances(reflections: List[Reflections]): List[AmfObject] = {
    reflections.flatMap { dir =>
      val classes = dir.getSubTypesOf(classOf[AmfObject]).asScala.toList
      classes.flatMap { clazz =>
        try {
          val instance = clazz.getMethod("apply")
            .invoke("") // ignored param as method is static
          Some(instance.asInstanceOf[AmfObject])
        } catch {
          case _: Throwable => None // case of abstract classes and interfaces
        }
      }
    }
  }

  private def callPlatformWrap(instance: AmfObject): Unit =
    try {
      platform.wrap(instance)
    } catch {
      case _: Throwable =>
        fail(s"The following model does not have a platform wrapper registered: ${instance.getClass.getName}")
    }
}
