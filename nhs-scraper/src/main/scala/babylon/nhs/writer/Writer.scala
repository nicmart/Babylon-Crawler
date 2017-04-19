package babylon.nhs.writer

import java.io.FileWriter

import babylon.nhs.serialiser.Serialiser

/**
  * Consume a value of type T causing a side-effect
  */
trait Writer[T] {
    def write(value: T): Unit
}

/**
  * An implementation of writer that uses a String writer and a serialiser for T
  */
class SerialiserWriter[T](serialiser: Serialiser[T], writer: Writer[String]) extends Writer[T] {
    def write(value: T): Unit = writer.write(serialiser.serialise(value))
}

/**
  * Println writer
  */
object PrintlnWriter extends Writer[String] {
    def write(value: String): Unit = println(value)
}

/**
  * String Writer based on JAVA's file writers.
  */
class JavaFileWriter(fileWriter: FileWriter) extends Writer[String] {
    def write(value: String): Unit = {
        fileWriter.write(value)
        fileWriter.flush()
        fileWriter.close()
    }
}