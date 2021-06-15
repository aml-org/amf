package amf.cli.internal.tasks.validations

import java.io.{BufferedWriter, FileWriter}

import amf.plugins.document.apicontract.validation.DefaultAMFValidations

object ValidationProfileExporter {

  def main(args: Array[String]): Unit = {
    DefaultAMFValidations.profiles().foreach { profile =>
      val generator = new ValidationDialectTextGenerator(profile)
      val text      = generator.emit()
      val writer = new BufferedWriter(
        new FileWriter(s"./documentation/validations/${profile.name.profile.toLowerCase}_profile.raml"))
      writer.write(text)
      writer.close()
    }
  }
}
