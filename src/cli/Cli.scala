package com.idkidknow.niriwatcher.cli

import cats.data.Validated
import cats.data.ValidatedNel
import cats.syntax.all.*
import com.monovore.decline.*

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

  private val view = Opts.argument[View](metavar = "view")

  private val niriSocket =
    Opts.env[String](name = "NIRI_SOCKET", help = "NIRI_SOCKET")

  final case class Print(
      niriSocket: String,
      view: View,
  )

  private val printCommand =
    Command[Print](name = "print", header = "print states to stdout") {
      (niriSocket, view).mapN(Print.apply)
    }

  val command: Command[Print] = Command(
    name = "niri-watcher",
    header = "restore states from niri event stream",
  ) {
    Opts.subcommand(printCommand)
  }
}
