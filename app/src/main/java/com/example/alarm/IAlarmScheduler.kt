package com.example.alarm

interface IAlarmScheduler {
    fun scheduleAlarm(occurrenceId: String, triggerTimeMillis: Long)
    fun cancelAlarm(occurrenceId: String)
    fun canScheduleExact(): Boolean
}
