package com.idkidknow.niriwatcher

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.std.Console
import cats.effect.std.Env
import cats.syntax.all.*

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = Env[IO].entries.flatMap {
    env =>
      cli.command.parse(args, env.toMap) match {
        case Left(help) if help.errors.nonEmpty =>
          Console[IO].errorln(help.show) *> ExitCode.Error.pure[IO]
        case Left(help) =>
          Console[IO].println(help.show) *> ExitCode.Success.pure[IO]
        case Right(io) => io
      }
  }
}
