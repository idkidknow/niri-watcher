package com.idkidknow.niriwatcher.event

import com.idkidknow.niriwatcher.util.UnsignedInstances.given
import io.circe.Codec

import scala.scalanative.unsigned.*

final case class Timestamp(secs: ULong, nanos: UInt) derives Codec
