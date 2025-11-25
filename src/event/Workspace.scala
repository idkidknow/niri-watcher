package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedInstances.given
import io.circe.Codec
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

import scala.scalanative.unsigned.*

final case class Workspace(
    id: ULong,
    idx: UByte,
    name: Option[String],
    output: Option[String],
    isUrgent: Boolean,
    isActive: Boolean,
    isFocused: Boolean,
    activeWindowId: Option[ULong],
)

object Workspace {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Codec[Workspace] = ConfiguredCodec.derived
}
