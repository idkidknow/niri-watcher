package com.idkidknow.niriwatcher.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.UnixSocketAddress
import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Config
import com.idkidknow.niriwatcher.state.KeyboardLayouts
import com.idkidknow.niriwatcher.state.Overview
import com.idkidknow.niriwatcher.state.Screenshot
import com.idkidknow.niriwatcher.state.StateBuilder
import com.idkidknow.niriwatcher.state.Windows
import com.idkidknow.niriwatcher.state.Workspaces
import com.monovore.decline.*
import fs2.Stream
import fs2.io.net.Network
import io.circe.syntax.*

def eventStream[F[_]: {Network, Concurrent, Console}](
    niriSocket: String
): Stream[F, Event] = for {
  socket <- Stream.resource(Network[F].connect(UnixSocketAddress(niriSocket)))
  request = Stream("\"EventStream\"\n")
    .through(fs2.text.utf8.encode)
    .through(socket.writes)
  read = socket.reads
    .through(fs2.text.utf8.decode)
    .through(fs2.text.lines)
    .drop(1) // the first line is {"Ok":"Handled"}
    .flatMap { s =>
      io.circe.parser.decode[Event](s) match {
        case Left(e) =>
          Stream.exec(
            Console[F].errorln(show"failed to parse the event: $s\nError: $e")
          )
        case Right(event) => Stream.emit(event)
      }
    }
  event <- request ++ read
} yield event

private def niriSocket =
  Opts.env[String](name = "NIRI_SOCKET", help = "NIRI_SOCKET")

private val workspaces =
  Command(name = "workspaces", header = "watch workspaces") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[Workspaces].accept)
        .map(_.asJson.noSpaces)
    }
  }
private val windows =
  Command(name = "windows", header = "watch windows") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[Windows].accept)
        .map(_.asJson.noSpaces)
    }
  }
private val keyboardLayouts =
  Command(name = "keyboardLayouts", header = "watch keyboard layouts") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[KeyboardLayouts].accept)
        .map(_.asJson.noSpaces)
    }
  }
private val config =
  Command(name = "config", header = "watch config loading") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[Config].accept)
        .map(_.asJson.noSpaces)
    }
  }
private val overview =
  Command(name = "overview", header = "watch overview") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[Overview].accept)
        .map(_.asJson.noSpaces)
    }
  }
private val screenshot =
  Command(name = "screenshot", header = "watch screenshot event") {
    niriSocket.map { socket =>
      eventStream[IO](socket)
        .through(StateBuilder[Screenshot].accept)
        .map(_.asJson.noSpaces)
    }
  }

private val printCommand =
  Command(name = "print", header = "print states to stdout") {
    Opts
      .subcommands(
        workspaces,
        windows,
        keyboardLayouts,
        config,
        overview,
        screenshot,
      )
      .map { s =>
        s.evalMap(line => IO.println(line)).compile.drain
          *> ExitCode.Success.pure[IO]
      }
  }

val command: Command[IO[ExitCode]] = Command(
  name = "niri-watcher",
  header = "restore states from niri event stream",
) {
  Opts.subcommand(printCommand)
}
