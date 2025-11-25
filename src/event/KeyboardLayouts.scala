package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedCodecs.given
import io.circe.Codec
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

import scala.scalanative.unsigned.*

final case class KeyboardLayouts(
    names: List[String],
    currentIdx: UByte,
)

object KeyboardLayouts {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Codec[KeyboardLayouts] = ConfiguredCodec.derived
}
