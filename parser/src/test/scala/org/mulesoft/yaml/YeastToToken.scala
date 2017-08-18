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
      if (args.length == 0) {
          val fileName = inputFile()
          val yeastFile = findGeneratedYeast()
          generate(yeastFile, new File(yeastDir, fileName + ".yt"))
      }
      else {

          val files = listYeastFiles()
          val targets = listTargetFiles(args(0), args(1).toInt, files.length)
          val ts = files.zip(targets).toList
          ts.foreach(t => generate(t._1, t._2))
      }
  }

    private def generate(yeastFile: File, ytFile: File) = {
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
    private def listYeastFiles() = {
        val strings = downloadDir.list()
        val ints: Array[Int] = strings.flatMap {
            case Pattern(n) => List(if (n == null) 0 else n.substring(1).toInt)
            case _ => Nil
        }.sorted(Ordering[Int])
        ints.map(n => if (n == 0) "index" else s"index-$n").map(name => new File(downloadDir, name + ".yeast"))
    }
    private def listTargetFiles(prefix:String, base:Int, n:Int) = {
        val range = base until base + n
        val missing = range.map(prefix + _ + ".yaml").filter(! new File(yamlDir, _).exists)
        if (missing.nonEmpty) {
            println(s"Missing files: $missing")
            System.exit(1)
        }
        range.map(n => new File(yeastDir, prefix + n + ".yt"))
    }

}

case class YeastData(token: YamlToken, start: Int, line: Int, col: Int, text: String) {
  override def toString: String = "%-15s %5d,%3d,%3d '%s'".format(token, start, line, col, text.encode)
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
