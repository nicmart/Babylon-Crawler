package babylon.nhs.service

import babylon.nhs.app.DefaultConfig
import io.circe._
import io.circe.generic.auto._, io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
object RestService {
    val config = DefaultConfig()
    val pages = config.loader.load().right.get.asJson

    def apply() = HttpService {

        case GET -> Root / "pages" =>
            Ok(pages).withType(MediaType.`application/json`)

    }
}
