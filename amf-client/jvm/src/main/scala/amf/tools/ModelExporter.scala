package amf.tools

import java.io.{File, FileWriter}

import amf.`export`.{Context, ExportableModelAdapter, MarkdownExporter, ModelTraverser}
import amf.core.metamodel.Obj
import org.reflections.Reflections

object ModelExporter {

  def exportText(startingModels: List[Obj] = getModelsByReflection): String = {
//    println(s"Starting model export...")
    val models = ModelTraverser.traverse(startingModels, new Context())
//    println("Traversed depended model...")
    val exportable = ExportableModelAdapter.adapt(models)
    MarkdownExporter.exportToMarkdown("AMF Model Documentation", exportable)
  }

  def exportTo(path: String, startingModels: List[Obj] = getModelsByReflection): Unit = {
    val exportedText = exportText(getModelsByReflection)
    withWriter(path) { writer =>
      writer.write(exportedText)
    }
//    println("Exported OK...")
  }

  def getModelsByReflection: List[Obj] = {
    val reflections = List(new Reflections("amf.plugins"), new Reflections("amf.core"))
    ObjLoader.loadObjs(reflections)
  }

  def withWriter(fileName: String)(write: FileWriter => Unit): Unit = {
    val file   = new File(fileName)
    val writer = new FileWriter(file)
    write(writer)
    writer.close()
  }
}

object ModelExporterProgram {
  val fileName = "model.md"
  val basePath = "documentation"

  def main(args: Array[String]): Unit = {
    ModelExporter.exportTo(s"${basePath}/${fileName}")
  }
}
