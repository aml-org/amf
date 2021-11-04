package amf.client.model

import amf.apicontract.client.platform.model.domain.api.WebApi
import amf.apicontract.client.scala.WebAPIConfiguration
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.ContextMapping
import amf.shapes.internal.domain.metamodel.{BaseIRIModel, ContextMappingModel, CuriePrefixModel, DefaultVocabularyModel, SemanticContextModel}
import org.reflections.Reflections
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConverters.asScalaSetConverter


class PlatformModelWrappersTest extends FunSuite with Matchers with PlatformSecrets {

  private val filtered = List(
    ContextMappingModel.`type`.head.toString,
    SemanticContextModel.`type`.head.toString,
    DefaultVocabularyModel.`type`.head.toString,
    CuriePrefixModel.`type`.head.toString,
    BaseIRIModel.`type`.head.toString
  )

  test("All models have platform wrappers registered") {
    WebAPIConfiguration.WebAPI() // registers all wrappers, remove when APIMF-3000 is done
    val reflections = List(
      new Reflections("amf.apicontract.client.scala.model"),
      new Reflections("amf.shapes.client.scala.model"),
      new Reflections("amf.aml.client.scala.model"),
      new Reflections("amf.core.client.scala.model"),
    )
    val instances = obtainModelInstances(reflections)
    instances.filter(doesntHaveClientWrappers).foreach(callPlatformWrap)
    succeed
  }

  private def doesntHaveClientWrappers(x: AmfObject) = {
    !filtered.contains(x.meta.`type`.head.toString)
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
