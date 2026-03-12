import sbt._

object SourceGenerators {

  def generateEmbeddedFileSource(
      inputFile: File,
      outputBaseDir: File,
      packageName: String,
      objectName: String
  ): Seq[File] = {
    val content      = IO.read(inputFile)
    val packagePath  = packageName.replace('.', '/')
    val outputDir    = outputBaseDir / packagePath
    val outputFile   = outputDir / s"$objectName.scala"
    // Split content into chunks smaller than 65535 bytes to avoid JVM constant pool limit
    val chunkSize    = 60000
    val chunks       = content.grouped(chunkSize).toSeq
    val chunksCode   = chunks.zipWithIndex.map { case (chunk, i) =>
      s"""  private val chunk$i = \"\"\"$chunk\"\"\""""
    }.mkString("\n")
    val joinCode     = chunks.indices.map(i => s"chunk$i").mkString(" + ")
    val scalaContent =
      s"""package $packageName
         |
         |// AUTO-GENERATED - DO NOT EDIT
         |// Generated from: ${inputFile.getPath}
         |
         |object $objectName {
         |$chunksCode
         |
         |  val content: String = $joinCode
         |}
         |""".stripMargin
    IO.write(outputFile, scalaContent)
    Seq(outputFile)
  }
}
