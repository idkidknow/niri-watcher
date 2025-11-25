package com.idkidknow.niriwatcher.state

import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.event.Workspace
import com.idkidknow.niriwatcher.state.Update.UpdateResult
import io.circe.Encoder
import monocle.syntax.all.*

import scala.scalanative.unsigned.*

opaque type Workspaces = Map[ULong, Workspace]

object Workspaces {
  def empty: Workspaces = Map.empty

  given Encoder[Workspaces] =
    Encoder.encodeList[Workspace].contramap(map => map.values.toList)

  given StateBuilder[Workspaces] = new StateBuilder {
    override def initialState: Map[ULong, Workspace] = Map.empty

    override def update(
        state: Map[ULong, Workspace],
        event: Event,
    ): UpdateResult[Map[ULong, Workspace]] = {
      import Event.*
      event match {
        case WorkspacesChanged(workspaces) =>
          UpdateResult.notify(
            workspaces.map(w => (w.id, w)).toMap
          )
        case WorkspaceUrgencyChanged(id, urgent) =>
          UpdateResult.notify(
            state.focus(_.index(id).isUrgent).replace(urgent)
          )
        case WorkspaceActivated(id: ULong, focused: Boolean) =>
          val output = state(id).output
          val updated = state.view.mapValues { w =>
            val isCurr = w.id == id
            val ret = if (w.output == output) w.copy(isActive = isCurr) else w
            if (focused) {
              ret.copy(isFocused = isCurr)
            } else ret
          }.toMap
          UpdateResult.notify(
            updated
          )
        case WorkspaceActiveWindowChanged(
              workspaceId: ULong,
              activeWindowId: Option[ULong],
            ) =>
          UpdateResult.notify(
            state
              .focus(_.index(workspaceId).activeWindowId)
              .replace(activeWindowId)
          )
        case _ => UpdateResult.dontNotify(state)
      }
    }
  }
}
