package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedInstances.given
import io.circe.Codec
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredCodec

import scala.scalanative.unsigned.*

final case class WindowLayout(
    posInScrollingLayout: Option[(USize, USize)],
    tileSize: (Double, Double),
    windowSize: (Int, Int),
    tilePosInWorkspaceView: Option[(Double, Double)],
    windowOffsetInTile: (Double, Double),
)

object WindowLayout {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Codec[WindowLayout] = ConfiguredCodec.derived
}
