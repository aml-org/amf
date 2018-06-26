package amf

import amf.client.convert.CoreRegister
import amf.core.AMFSerializer
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Context, RamlYamlHint}
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.plugins.document.{Vocabularies, WebApi}
import amf.plugins.features.AMFValidation

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object IntellijTest extends PlatformSecrets with App {
  override def main(args: Array[String]): Unit = {
    val productionPath = "amf-client/shared/src/test/resources/production/"
    val fileName       = "s-suez-delivery-collection-api-1.0.0-fat-raml/api.raml"
    val file           = s"file://$productionPath$fileName"
    val hint           = RamlYamlHint
    val target         = Amf
    println("STARTING")

    ExecutionLog.start()

    WebApi.register()
    Vocabularies.register()
    AMFValidation.register()
    CoreRegister.register(platform)
    val validation = new Validation(platform)

    val ff = validation.init() map { _ =>
      // Init the core component
      val f = amf.core.AMF.init().flatMap { _ =>
        validation.init().map(_ => validation)
        RuntimeValidator.reset()
        println("** parsing")
        RuntimeCompiler(file, Option("application/yaml"), "RAML 0.8", Context(platform))
      } flatMap { model =>
        println("** validating")
        validation.validate(model, RAML08Profile) map { report =>
          println("** validation report")
          println(report)
          model
        }
      } flatMap { model =>
        println("** serialising")
        new AMFSerializer(model, "application/ld+json", "AMF Graph", RenderOptions()).renderToString
      } map { text =>
        //println(text)
        println("PARSED!!!")
      }

      Await.result(f, Duration.Inf)
    }
    Await.result(ff, Duration.Inf)
  }
}
