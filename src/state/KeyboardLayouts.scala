package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event
import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder

opaque type KeyboardLayouts = Option[event.KeyboardLayouts]

object KeyboardLayouts {
  given Encoder[KeyboardLayouts] = Encoder.encodeOption[event.KeyboardLayouts]

  given StateBuilder[KeyboardLayouts] = new StateBuilder {
    override def initialState: KeyboardLayouts = None

    override def update(
        state: KeyboardLayouts,
        event: Event,
    ): UpdateResult[KeyboardLayouts] = {
      import Event.*
      event match {
        case KeyboardLayoutsChanged(keyboardLayouts) =>
          UpdateResult.notify(Some(keyboardLayouts))
        case KeyboardLayoutSwitched(idx) =>
          UpdateResult.notify(state.map(_.copy(currentIdx = idx)))
        case _ => UpdateResult.dontNotify(state)
      }
    }
  }
}
