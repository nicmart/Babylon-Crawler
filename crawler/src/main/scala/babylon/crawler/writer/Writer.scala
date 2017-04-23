package babylon.crawler.writer

import java.io.FileWriter

import io.circe.Encoder

/**
  * Consume a value of type T causing a side-effect
  */
trait Writer[T] {
    def write(value: T): Unit
}

object Writer {
    def apply[T](f: T => Unit): Writer[T] = new Writer[T] {
        def write(value: T): Unit = f(value)
    }
}

/**
  * An implementation of writer that uses a String writer and a json encoder for T
  */
class JsonWriter[T](encoder: Encoder[T], writer: Writer[String]) extends Writer[T] {
    def write(value: T): Unit = writer.write(encoder(value).toString())
}

/**
  * Println writer, mainly used for debugging
  */
object PrintlnWriter extends Writer[String] {
    def write(value: String): Unit = println(value)
}

/**
  * String Writer based on JAVA's file writers.
  */
class JavaFileWriter(fileWriter: => FileWriter) extends Writer[String] {
    lazy val javaWriter = fileWriter
    def write(value: String): Unit = {
        javaWriter.write(value)
        javaWriter.flush()
        javaWriter.close()
    }
}