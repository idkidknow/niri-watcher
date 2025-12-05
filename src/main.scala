package com.idkidknow.niriwatcher

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.effect.std.Env
import cats.syntax.all.*
import com.comcast.ip4s.UnixSocketAddress
import com.idkidknow.niriwatcher.cli.Cli
import com.idkidknow.niriwatcher.event.Event
import com.idkidknow.niriwatcher.state.Config
import com.idkidknow.niriwatcher.state.KeyboardLayouts
import com.idkidknow.niriwatcher.state.Overview
import com.idkidknow.niriwatcher.state.Screenshot
import com.idkidknow.niriwatcher.state.StateBuilder
import com.idkidknow.niriwatcher.state.Windows
import com.idkidknow.niriwatcher.state.Workspaces
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

def stateStream(niriSocket: String, view: Cli.View): Stream[IO, String] = {
  val events = eventStream[IO](niriSocket)
  view match {
    case "workspaces" =>
      events
        .through(StateBuilder[Workspaces].accept)
        .map(_.asJson.noSpaces)
    case "windows" =>
      events
        .through(StateBuilder[Windows].accept)
        .map(_.asJson.noSpaces)
    case "keyboard_layouts" =>
      events
        .through(StateBuilder[KeyboardLayouts].accept)
        .map(_.asJson.noSpaces)
    case "config" =>
      events
        .through(StateBuilder[Config].accept)
        .map(_.asJson.noSpaces)
    case "overview" =>
      events
        .through(StateBuilder[Overview].accept)
        .map(_.asJson.noSpaces)
    case "screenshot" =>
      events
        .through(StateBuilder[Screenshot].accept)
        .map(_.asJson.noSpaces)
  }
}

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = Env[IO].entries.flatMap {
    env =>
      val env1 = env.toMap
      Cli.command.parse(args, env1) match {
        case Left(help) if help.errors.nonEmpty =>
          Console[IO].errorln(help.show) *> ExitCode.Error.pure[IO]
        case Left(help) =>
          Console[IO].println(help.show) *> ExitCode.Success.pure[IO]
        case Right(print: Cli.Print) =>
          stateStream(print.niriSocket, print.view)
            .evalMap(s => IO.println(s))
            .compile
            .drain *> ExitCode.Success.pure[IO]
      }
  }
}
