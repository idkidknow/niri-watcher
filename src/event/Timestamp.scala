package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedCodecs.given
import io.circe.Codec

import scala.scalanative.unsigned.*

final case class Timestamp(secs: ULong, nanos: UInt) derives Codec
