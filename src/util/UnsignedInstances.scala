package com.idkidknow.niriwatcher.util

import cats.kernel.Eq
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsigned.*

object UnsignedInstances {
  given u8Codec: Codec[UByte] = Codec.from(
    encodeA = Encoder[Short].contramap(n => n.toShort),
    decodeA = Decoder[Short].emap {
      case n if n < 0 || n.toUShort > UByte.MaxValue =>
        Left("number out of range")
      case n => Right(n.toUByte)
    },
  )

  given u16Codec: Codec[UShort] = Codec.from(
    encodeA = Encoder[Int].contramap(n => n.toInt),
    decodeA = Decoder[Int].emap {
      case n if n < 0 || n.toUInt > UShort.MaxValue =>
        Left("number out of range")
      case n => Right(n.toUShort)
    },
  )

  given u32Codec: Codec[UInt] = Codec.from(
    encodeA = Encoder[Long].contramap(n => n.toLong),
    decodeA = Decoder[Long].emap {
      case n if n < 0 || n.toULong > UInt.MaxValue =>
        Left("number out of range")
      case n => Right(n.toUInt)
    },
  )

  given u64Codec: Codec[ULong] = Codec.from(
    encodeA = Encoder[BigInt].contramap(n => BigInt(n.toString)),
    decodeA = Decoder[BigInt].emap {
      case n if n < 0 || n > BigInt(ULong.MaxValue.toString) =>
        Left("number out of range")
      case n => Right(n.toLong.toULong)
    },
  )

  given usizeCodec: Codec[USize] = if (LinktimeInfo.is32BitPlatform) {
    Codec.from(u32Codec.map(n => n.toUSize), u32Codec.contramap(n => n.toUInt))
  } else {
    Codec.from(u64Codec.map(n => n.toUSize), u64Codec.contramap(n => n.toULong))
  }

  given u8Eq: Eq[UByte] = Eq.fromUniversalEquals
  given u16Eq: Eq[UShort] = Eq.fromUniversalEquals
  given u32Eq: Eq[UInt] = Eq.fromUniversalEquals
  given u64Eq: Eq[ULong] = Eq.fromUniversalEquals
  given usizeEq: Eq[USize] = Eq.fromUniversalEquals
}
