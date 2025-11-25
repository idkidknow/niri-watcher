package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.event.Window
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder
import monocle.syntax.all.*

import scala.scalanative.unsigned.*

opaque type Windows = Map[ULong, Window]

object Windows {
  def empty: Windows = Map.empty

  given Encoder[Windows] =
    Encoder.encodeList[Window].contramap(map => map.values.toList)

  given StateBuilder[Windows] = new StateBuilder {
    override def initialState: Map[ULong, Window] = Map.empty

    override def update(
        state: Map[ULong, Window],
        event: Event,
    ): UpdateResult[Map[ULong, Window]] = {
      import Event.*
      event match {
        case WindowsChanged(windows) =>
          UpdateResult.notify(
            windows.map(w => (w.id, w)).toMap
          )
        case WindowOpenedOrChanged(window) =>
          val inserted = state.updated(window.id, window)
          val ret = if (window.isFocused) {
            inserted.mapValues { w =>
              w.copy(isFocused = w.id == window.id)
            }.toMap
          } else {
            inserted
          }
          UpdateResult.notify(ret)
        case WindowClosed(id) =>
          UpdateResult.notify(state.removed(id))
        case WindowFocusChanged(id) =>
          UpdateResult.notify(state.mapValues { w =>
            w.copy(isFocused = Some(w.id) == id)
          }.toMap)
        case WindowFocusTimestampChanged(id, focusTimestamp) =>
          UpdateResult.notify(
            state.focus(_.index(id).focusTimestamp).replace(focusTimestamp)
          )
        case WindowUrgencyChanged(id, urgent) =>
          UpdateResult.notify(
            state.focus(_.index(id).isUrgent).replace(urgent)
          )
        case WindowLayoutsChanged(changes) =>
          val changesMap = changes.toMap
          UpdateResult.notify(
            state.mapValues { w =>
              changesMap.get(w.id) match {
                case Some(layout) => w.copy(layout = layout)
                case None => w
              }
            }.toMap
          )
        case _ => UpdateResult.dontNotify(state)
      }
    }
  }
}
