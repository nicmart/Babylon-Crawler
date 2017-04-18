package babylon.nhs.app

import java.io.File

import babylon.nhs.config.Config
import babylon.nhs.loader.JsonPageListLoader
import babylon.nhs.output.Output.PageList
import io.circe.Decoder

/**
  * Created by Nicolò Martini on 18/04/2017.
  */
object DefaultConfig {
    def apply()(implicit decoder: Decoder[PageList]): Config =
        Config(
            loader = new JsonPageListLoader(
                decoder,
                scala.io.Source.fromFile(
                    new File("cache/pages.json")
                )
            )
        )
}
