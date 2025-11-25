package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredEncoder

final case class Overview(
    isOpen: Boolean
)

object Overview {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Encoder[Overview] = ConfiguredEncoder.derived

  given StateBuilder[Overview] = new StateBuilder {
    override def initialState: Overview = Overview(false)

    override def update(state: Overview, event: Event): UpdateResult[Overview] =
      event match {
        case Event.OverviewOpenedOrClosed(isOpen) =>
          UpdateResult.notify(Overview(isOpen))
        case _ => UpdateResult.dontNotify(state)
      }
  }
}
