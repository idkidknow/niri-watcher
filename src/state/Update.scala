package com.idkidknow.niriwatcher.state

import cats.kernel.Monoid
import com.idkidknow.niriwatcher.event.Event

trait Update[A] {

  /** Update the state according to the event. */
  def update(state: A, event: Event): Update.UpdateResult[A]
}

object Update {
  final case class UpdateResult[A](
      updated: A,
      shouldNotify: Boolean,
  )

  object UpdateResult {
    def notify[A](updated: A): UpdateResult[A] = UpdateResult(updated, true)

    /** Unchanged or internal state changed so no need to notify users */
    def dontNotify[A](prev: A): UpdateResult[A] = UpdateResult(prev, false)
  }

  def apply[A: Update]: Update[A] = summon

  given monoidUpdate[A]: Monoid[Update[A]] = new Monoid {
    override def empty: Update[A] = (state, _) => UpdateResult.dontNotify(state)
    override def combine(x: Update[A], y: Update[A]): Update[A] =
      (state, event) => {
        val xRet = x.update(state, event)
        val state1 = xRet.updated
        val yRet = y.update(state1, event)
        val finalState = yRet.updated
        UpdateResult(finalState, xRet.shouldNotify || yRet.shouldNotify)
      }
    override def combineAll(as: IterableOnce[Update[A]]): Update[A] =
      (state, event) => {
        val (a, shouldNotify) = as.iterator.foldLeft((state, false)) {
          case ((currState, shouldNotify), update) =>
            val ret = update.update(currState, event)
            (ret.updated, shouldNotify || ret.shouldNotify)
        }
        UpdateResult(a, shouldNotify)
      }
  }
}
