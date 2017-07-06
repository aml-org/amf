package org.mulesoft.yaml

import java.io.{File, PrintWriter}

import org.mulesoft.common.core.Strings
import org.mulesoft.lexer.TokenData
import org.yaml.lexer.YamlToken

import scala.io.{Source, StdIn}
import scala.util.matching.Regex


/**
  * Created by emilio.gabeiras on 6/9/17.
  */
object YeastToYToken {
    private final val userDir = System.getenv("HOME")
    private final val downloadDir = new File(userDir, "Downloads")
    private final val dataDir = s"$userDir/projects/amf/parser/src/test/data"
    private final val yamlDir = new File(dataDir, "yaml")
    private final val yeastDir = new File(dataDir, "yeast")
    private val Pattern = new Regex("index(-[0-9]+)?.yeast")

  def main(args: Array[String]): Unit = {
     val fileName = inputFile()
      val yeastFile = findGeneratedYeast()
      val ytFile = new File(yeastDir, fileName + ".yt")

      val output = new PrintWriter(ytFile)
      for (Seq(a, b) <- Source.fromFile(yeastFile).getLines().grouped(2))
        output.println(YeastData(a + ", " + b))
      output.close()
  }

    private def inputFile():String = {
        while (true) {
            var s = StdIn.readLine("Enter File Name (No Extension): ")
            if (Character.isDigit(s(0))) s = "example-" + s
            if (new File(yamlDir, s + ".yaml").exists()) return s
            println("File does not exists")
        }
        ""
    }
    private def findGeneratedYeast() = {
        val strings = downloadDir.list()
        val last = strings.flatMap {
            case Pattern(n) => List(if (n == null) 0 else n.substring(1).toInt)
            case _ => Nil
        }.max
        val name = if (last == 0) "index" else s"index-$last"
        new File(downloadDir, name + ".yeast")
    }
}

case class YeastData(t: YamlToken, start: Int, line: Int, col: Int, text: String) {
  override def toString: String = "%-15s %5d,%3d,%3d '%s'".format(t, start, line, col, text.encode)
}

object YeastData {
  def apply(tokenData: TokenData[YamlToken], txt: String): YeastData =
    new YeastData(tokenData.token,
                  tokenData.start,
                  tokenData.range.lineFrom,
                  tokenData.range.columnFrom,
                  txt)
  def apply(str: String): YeastData = {
    str match {
      case Regex(_, offset, line, column, abbreviation, text) =>
        new YeastData(YamlToken(abbreviation), offset.toInt, line.toInt, column.toInt, text.decode)
    }
  }
  private val Regex = "# B: (\\d+), C: (\\d+), L: (\\d+), c: (\\d+), ([a-zA-Z-])?(.*)".r
}
