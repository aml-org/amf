package org.mulesoft.yaml

import java.io.{File, PrintWriter}

import org.scalatest.{FunSuite, Matchers}
import org.yaml.parser.YamlParser

/**
  * Test against golden files
  */
class YamlToModelTest extends FunSuite with Matchers {

  val modelDir  = new File("target/test/model")
  val yamlDir   = new File("src/test/data/yaml")
  val goldenDir = new File("src/test/data/yeast")

  modelDir.mkdirs()

  private val file  = System.getProperty("yaml")
  private val files = if (file == null) yamlDir.list() else Array(file)
  for (yaml <- files) { //}; if !yaml.equals("simplelist.yaml")) {
    test("Generate Yaml Model for " + yaml) {
      val yamlFile   = new File(yamlDir, yaml)
      val yeast      = yaml.replace(".yaml", ".ym")
      val yeastFile  = new File(modelDir, yeast)
      val goldenFile = new File(goldenDir, yeast)

      generate(yamlFile, yeastFile)


      //   val deltas = Diff.ignoreAllSpace.diff(yeastFile, goldenFile)

      //    assert(deltas.isEmpty, s"diff -y -W 150 $yeastFile $goldenFile\n\n${deltas.mkString}")

    }
  }

  private def generate(yamlFile: File, yeastFile: File) = {
  //  val out    = new PrintWriter(yeastFile)
    val elements = YamlParser(yamlFile).parse()
    for (e <- elements) {
      println(e)
      println("=======================")
    }
    //out.close()
  }

}
