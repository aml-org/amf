package amf

import java.io.File

import amf.core.remote.RamlYamlHint
import amf.io.BuildCycleTests

class Raml2ParserTest() extends BuildCycleTests {
  override val basePath: String =
    "/Users/hernan.najles/mulesoft/amf/amf-client/shared/src/test/resources/raml-parser/v2/"

  def getFiles(path: String): List[File] = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
      val (files, directories) = d.listFiles.toList.partition(_.isFile)

      files ++ directories.flatMap(d => getFiles(d.getPath))
    } else List()
  }

  getFiles(basePath).foreach(f => {
    test("testing file " + f.getAbsoluteFile) {
      cycle(f.getName, RamlYamlHint, directory = f.getParent + "/")
    }
  })

}
