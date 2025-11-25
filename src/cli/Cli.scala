package com.idkidknow.niriwatcher.cli

import cats.data.Validated
import cats.data.ValidatedNel
import cats.syntax.all.*
import com.monovore.decline.*

import scala.concurrent.duration.*

object Cli {
  type View = "workspaces" | "windows" | "keyboard_layouts" | "config" |
    "overview" | "screenshot"
  given Argument[View] = new Argument {
    override def defaultMetavar: String = "view"
    override def read(string: String): ValidatedNel[String, View] =
      string match {
        case v: View => Validated.valid(v)
        case _ => Validated.invalidNel(s"Invalid view: $string")
      }
  }

  val view = Opts.argument[View](metavar = "view")

  val niriSocket = Opts.env[String](name = "NIRI_SOCKET", help = "NIRI_SOCKET")

  final case class Print(
      niriSocket: String,
      view: View,
  )

  val printCommand =
    Command[Print](name = "print", header = "print states to stdout") {
      (niriSocket, view).mapN(Print.apply)
    }

  final case class Watch(
      niriSocket: String,
      view: View,
      exe: String,
      args: List[String],
      timeout: FiniteDuration,
  )

  val exe = Opts.argument[String](metavar = "exe")

  val args = Opts.arguments[String](metavar = "args").orEmpty

  val timeout =
    Opts
      .option[Int](
        long = "timeout",
        help = "subprocess timeout (millis) [default: 1000]",
        metavar = "millis",
      )
      .withDefault(1000)
      .map(int => int.millis)

  val watchCommand = Command[Watch](
    name = "watch",
    header =
      "run subprocess with environment variable STATE when the state changed",
  ) {
    (niriSocket, view, exe, args, timeout).mapN(Watch.apply)
  }

  val command = Command[Print | Watch](
    name = "niri-watcher",
    header = "restore states from niri event stream",
  ) {
    Opts.subcommand(printCommand) orElse Opts.subcommand(watchCommand)
  }
}
