package babylon.nhs.writer

import java.io.FileWriter

import babylon.nhs.serialiser.Serialiser

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
trait Writer[T] {
    def write(value: T): Unit
}

class SerialiserWriter[T](serialiser: Serialiser[T], writer: Writer[String]) extends Writer[T] {
    def write(value: T): Unit = writer.write(serialiser.serialise(value))
}

object PrintlnWriter extends Writer[String] {
    def write(value: String): Unit = println(value)
}

class JavaFileWriter(fileWriter: FileWriter) extends Writer[String] {
    def write(value: String): Unit = {
        fileWriter.write(value)
        fileWriter.flush()
        fileWriter.close()
    }
}