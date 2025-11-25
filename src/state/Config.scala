package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredEncoder

final case class Config(
    failed: Boolean
)

object Config {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Encoder[Config] = ConfiguredEncoder.derived

  given StateBuilder[Config] = new StateBuilder {
    override def initialState: Config = Config(false)

    override def update(state: Config, event: Event): UpdateResult[Config] =
      event match {
        case Event.ConfigLoaded(failed) =>
          UpdateResult.notify(Config(failed))
        case _ => UpdateResult.dontNotify(state)
      }
  }
}
