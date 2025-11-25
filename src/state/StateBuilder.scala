package com.idkidknow.niriwatcher.state

import cats.syntax.all.*
import com.idkidknow.niriwatcher.event.Event
import fs2.Pipe

trait StateBuilder[A] extends Update[A] {
  def initialState: A

  def accept[F[_]]: Pipe[F, Event, A] = events => {
    events
      .scan((initialState, false)) { case ((state, _), event) =>
        val ret = update(state, event)
        (ret.updated, ret.shouldNotify)
      }
      .mapFilter { case (state, shouldNotify) =>
        if (shouldNotify) Some(state) else None
      }
  }
}

object StateBuilder {
  def apply[A: StateBuilder]: StateBuilder[A] = summon
}
