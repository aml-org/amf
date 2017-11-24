package amf.tasks.validations

import java.io.{BufferedWriter, FileWriter}

import amf.plugins.features.validation.model.DefaultAMFValidations

object ValidationProfileExporter {

  def main(args: Array[String]): Unit = {
    DefaultAMFValidations.profiles().foreach { profile =>
      val generator = new ValidationDialectTextGenerator(profile)
      val text = generator.emit()
      val writer = new BufferedWriter(new FileWriter(s"./documentation/validations/${profile.name.toLowerCase}_profile.raml"))
      writer.write(text)
      writer.close()
    }
  }
}
