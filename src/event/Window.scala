package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedCodecs.given
import io.circe.Codec
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

import scala.scalanative.unsigned.*

final case class Window(
    id: ULong,
    title: Option[String],
    appId: Option[String],
    pid: Option[Int],
    workspaceId: Option[ULong],
    isFocused: Boolean,
    isFloating: Boolean,
    isUrgent: Boolean,
    layout: WindowLayout,
    focusTimestamp: Option[Timestamp],
)

object Window {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Codec[Window] = ConfiguredCodec.derived
}
