package babylon.nhs.serialiser

import babylon.nhs.output.Output.PageList

import io.circe.generic.auto._, io.circe.syntax._

/**
  * A conversion of a type T to a string
  */
trait Serialiser[T] {
    def serialise(value: T): String
}

/**
  * Default serialiser based on CIRCE
  */
object JsonCirceSerialiser extends Serialiser[PageList] {
    def serialise(value: PageList): String = value.asJson.toString()
}
