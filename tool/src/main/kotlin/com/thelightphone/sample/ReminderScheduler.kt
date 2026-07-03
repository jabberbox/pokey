package com.thelightphone.sample

import com.thelightphone.sdk.LightWork
import com.thelightphone.sdk.SealedLightContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

const val SHOT_REMINDER_JOB_KEY = "shot-reminder"

/** How long after a shot the reminder should fire, per PROJECT.md's 7-day cycle. */
val REMINDER_CYCLE: Duration = 7.days

/**
 * How often WorkManager actually invokes the check job.
 *
 * LightWork.enqueuePeriodic has no initial-delay parameter, and — critically —
 * a PeriodicWorkRequest's first run happens almost immediately after enqueue,
 * not after one interval has elapsed. So this can't just be set to
 * [REMINDER_CYCLE] and left alone (that fired the reminder within seconds of
 * every shot log, immediately). Instead the job runs frequently, at
 * WorkManager's periodic minimum, and the handler itself decides whether the
 * real 7-day cycle has actually elapsed since the latest shot.
 */
private val CHECK_INTERVAL: Duration = 15.minutes

object ReminderScheduler {
    /**
     * Idempotent — safe to call on every shot log or app open. Uses
     * enqueuePeriodic's UPDATE policy, so it won't disrupt an already-running
     * schedule. No "reset" step is needed when a new shot is logged: the job
     * re-reads the latest shot from the database on every tick, so it
     * naturally picks up the new date on its next run.
     */
    fun ensureScheduled(lightContext: SealedLightContext) {
        LightWork.enqueuePeriodic(lightContext, SHOT_REMINDER_JOB_KEY, repeatInterval = CHECK_INTERVAL)
    }
}
