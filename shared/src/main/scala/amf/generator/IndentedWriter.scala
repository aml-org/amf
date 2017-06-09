package amf.generator

import amf.common.Strings.strings

/**
  * Created by pedro.colunga on 5/30/17.
  */
class IndentedWriter {

    def quoted(content: String): IndentedWriter = {
        write(content.quote)
    }

    /** Increase indentation. */
    def indent(): IndentedWriter = {
        indents += 1
        this
    }

    /** Decrease indentation. */
    def outdent(): IndentedWriter = {
        indents -= 1
        this
    }

    /** Start new line. */
    def line(): IndentedWriter = {
        begin = true
        builder.append('\n')
        this
    }

    def write(c: Char): IndentedWriter = {
        indentation()
        builder.append(c)
        this
    }

    def write(s: String): IndentedWriter = {
        indentation()
        builder.append(s)
        this
    }

    /** Print indentation. */
    private def indentation(): IndentedWriter = {
        if (begin) {
            (0 until indents).foreach(_ => builder.append("  "))
            begin = false
        }
        this
    }


    override def toString: String = builder.toString

    private val builder: StringBuilder = new StringBuilder()
    private var indents = 0
    private var begin = false
}
