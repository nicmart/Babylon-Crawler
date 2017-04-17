package babylon.nhs.serialiser

import babylon.nhs.actor.StorageActor
import babylon.nhs.output.Output.PageList

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

/**
  * Created by Nicolò Martini on 17/04/2017.
  */
trait Serialiser[T] {
    def serialise(value: T): String
}

object JsonStorageSerialiser extends Serialiser[PageList] {
    def serialise(value: PageList): String = value.asJson.toString()
}
