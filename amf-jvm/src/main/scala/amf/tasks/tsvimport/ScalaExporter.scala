package amf.tasks.tsvimport

import java.io.{BufferedWriter, FileWriter}

object ScalaExporter {

  def main(args: Array[String]): Unit = {
    val jsonld = ValidationsImporter.toScala
    val writer = new BufferedWriter(new FileWriter("./shared/src/main/scala/amf/validation/model/AMFRawValidations.scala"))
    writer.write(jsonld)
    writer.close()
  }

}
