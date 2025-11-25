package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredEncoder

final case class Screenshot(
    lastPath: Option[String]
)

object Screenshot {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Encoder[Screenshot] = ConfiguredEncoder.derived
  given StateBuilder[Screenshot] = new StateBuilder {
    override def initialState: Screenshot = Screenshot(None)

    override def update(
        state: Screenshot,
        event: Event,
    ): UpdateResult[Screenshot] =
      event match {
        case Event.ScreenshotCaptured(lastPath) =>
          UpdateResult.notify(Screenshot(lastPath))
        case _ => UpdateResult.dontNotify(state)
      }
  }
}
