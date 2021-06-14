package amf.tools

import amf.`export`.{Context, ExportableModelAdapter, MarkdownExporter, ModelTraverser}
import amf.core.internal.metamodel.Obj
import org.reflections.Reflections

import java.io.{File, FileWriter}

object ModelExporter {

  def exportText(startingModels: List[Obj] = getModelsByReflection): String = {
    val models     = ModelTraverser.traverse(startingModels, new Context())
    val exportable = ExportableModelAdapter.adapt(models)
    MarkdownExporter.exportToMarkdown("AMF Model Documentation", exportable)
  }

  def exportTo(path: String, startingModels: List[Obj] = getModelsByReflection): Unit = {
    val exportedText = exportText(startingModels)
    withWriter(path) { writer =>
      writer.write(exportedText)
    }
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
