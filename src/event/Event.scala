package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedCodecs.given
import io.circe.Codec
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

import scala.scalanative.unsigned.*

enum Event {
  case WorkspacesChanged(workspaces: List[Workspace])
  case WorkspaceUrgencyChanged(id: ULong, urgent: Boolean)
  case WorkspaceActivated(id: ULong, focused: Boolean)
  case WorkspaceActiveWindowChanged(
      workspaceId: ULong,
      activeWindowId: Option[ULong],
  )
  case WindowsChanged(windows: List[Window])
  case WindowOpenedOrChanged(window: Window)
  case WindowClosed(id: ULong)
  case WindowFocusChanged(id: Option[ULong])
  case WindowFocusTimestampChanged(id: ULong, focusTimestamp: Option[Timestamp])
  case WindowUrgencyChanged(id: ULong, urgent: Boolean)
  case WindowLayoutsChanged(changes: List[(ULong, WindowLayout)])
  case KeyboardLayoutsChanged(keyboardLayouts: KeyboardLayouts)
  case KeyboardLayoutSwitched(idx: UByte)
  case OverviewOpenedOrClosed(isOpen: Boolean)
  case ConfigLoaded(failed: Boolean)
  case ScreenshotCaptured(path: Option[String])
}

object Event {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Codec[Event] = ConfiguredCodec.derived
}
