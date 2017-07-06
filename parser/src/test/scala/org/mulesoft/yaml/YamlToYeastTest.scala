package org.mulesoft.yaml

import java.io.{File, PrintWriter}

import org.mulesoft.common.ext.Diff
import org.scalatest.{FunSuite, Matchers}
import org.yaml.lexer.{YamlLexer, YamlToken}

/**
  * Test against golden files
  */
class YamlToYeastTest extends FunSuite with Matchers {

  val yeastDir  = new File("target/test/yeast")
  val yamlDir   = new File("src/test/data/yaml")
  val goldenDir = new File("src/test/data/yeast")

  yeastDir.mkdirs()

  private val file  = System.getProperty("yaml")
  private val files = if (file == null) yamlDir.list() else Array(file)
  for (yaml <- files) { //}; if !yaml.equals("simplelist.yaml")) {
    test("Generate Yeast for " + yaml) {
      val yamlFile   = new File(yamlDir, yaml)
      val yeast      = yaml.replace(".yaml", ".yt")
      val yeastFile  = new File(yeastDir, yeast)
      val goldenFile = new File(goldenDir, yeast)

      generate(yamlFile, yeastFile)

      val deltas = Diff.ignoreAllSpace.diff(yeastFile, goldenFile)

      assert(deltas.isEmpty, s"diff -y -W 150 $yeastFile $goldenFile\n\n${deltas.mkString}")

    }
  }

  private def generate(yamlFile: File, yeastFile: File) = {
    val out   = new PrintWriter(yeastFile)
    val lexer = YamlLexer(yamlFile)
    while (lexer.token != YamlToken.EndStream) {
      val data = YeastData(lexer.tokenData, lexer.tokenString)
      out.println(data)
      lexer.advance()
    }
    out.close()
  }
}
