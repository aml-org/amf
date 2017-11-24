package amf.tasks.tsvimport

import java.io.File

/**
  * Imports validations from provided TSV files, generating SHACL shapes for each line in the files
  */
object ValidationsImporter {

  /**
    * Resource containing all the validations, they must be added in TSV format with a header
    * @return File
    */
  protected def validationsFolder: File = {
    new File(getClass.getResource("/validations").getPath)
  }

  /**
    * List of files stored in the resources of the project
    * @return List[File]
    */
  protected def validationFiles(): List[ValidationsFile] = {
    val folder = validationsFolder
    if (folder.exists && folder.isDirectory) {
      folder.listFiles.toList
        .map(new ValidationsFile(_))
    } else {
      List[ValidationsFile]()
    }
  }

  /**
    * Returns the code for a Scala Object containing all the validation lines
    * @return
    */
  def toScala: String = {
    val validations: List[String] = validationFiles().flatMap(_.lines())
    val lines = validations
      .map { (line) =>
        "\t\"" + line + "\""
      }
      .reduce(_ + ",\n" + _)

    s"""
       |// auto-generated class from ValidationsImporter.toScala
       |package amf.plugins.features.validation.model
       |
       |// scalastyle:off line.contains.tab
      |object AMFRawValidations {
       |  val raw = List(
       |  $lines
       |  )
       |}
    """.stripMargin
  }
}
