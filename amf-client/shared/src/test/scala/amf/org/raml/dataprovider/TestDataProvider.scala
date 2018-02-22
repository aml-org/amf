package amf.org.raml.dataprovider

abstract class TestDataProvider {

//  def getData(baseFolder: String, inputFileName: String, outputFileName: String): List[Array[AnyRef]] = scanPath("", baseFolder, inputFileName, outputFileName)
//
//  private def scanPath(folderPath: String, baseFolder: String, inputFileName: String, outputFileName: String) = {
//    val testFolder = Fs.syncFile(baseFolder)
//    val scenarios: Id[Array[String]] = testFolder.list
//    val result = ListBuffer[Array[AnyRef]]()
//    for (scenario <- scenarios) {
//      val value = Fs.syncFile(scenario)
//      if(value.isDirectory){
//        val input = Fs.asyncFile(value, inputFileName)
//        val output = Fs.asyncFile(value, outputFileName)
//
//        if (input.isFile && (output.isFile || existsOutputIgnoreFile(value, outputFileName))) result += List(input, output, folderPath + value.name).toArray
//        else if (value.list.nonEmpty) result ++= scanPath(folderPath + value.name + ".", value.path, inputFileName, outputFileName)
//      }
//    }
//    result
//
//  }
//
//
//  private def existsOutputIgnoreFile(scenario: SyncFile, outputFileName: String): Boolean = Fs.syncFile(scenario, outputFileName + ".ignore").isFile
}
