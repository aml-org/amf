package amf.facades

import amf.RAMLProfile
import amf.core.benchmark.ExecutionLog
import amf.core.remote.RamlYamlHint
import amf.core.unsafe.PlatformSecrets

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Benchmark extends PlatformSecrets {

  val warmup: String = "file://amf-client/shared/src/test/resources/validations/resource_types/resource_type1.raml"
  // val api: String = "file://amf-client/shared/src/test/resources/validations/examples/object-name-example.raml"
  val api: String = "file://amf-client/shared/src/test/resources/production/getsandbox.comv1swagger.raml"
  //val api: String = "file://amf-client/shared/src/test/resources/production/financial-api/infor-financial-api.raml"

  def main(args: Array[String]): Unit = {
    val running = for {
      i <- Range(0, 5)
    } yield {
      val file = if (i == 0) {
        println(s"run $i ====> WARMUP")
        warmup
      } else {
        println(s"run $i")
        api
      }

      ExecutionLog.start()
      val current = for {
        validation <- Validation(platform)
        model      <- AMFCompiler(file, platform, RamlYamlHint, validation).build()
        report     <- validation.validate(model, RAMLProfile)
      } yield {
        ExecutionLog.finish()
      }
      Await.result(current, Duration.Inf)
    }
    println("All runs finished")
    ExecutionLog.buildReport()
  }

}
