package org.cpardi.messagemirror.services

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServiceManager(private val context: Context) {
    fun refresh() {
        val workName = "OneTimeUniqueWork"
        val workManager = WorkManager.Companion.getInstance(context)
        val startServiceRequest = OneTimeWorkRequest.Builder(ServiceStartWorker::class.java).build()
        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, startServiceRequest)
    }

    class ServiceStartWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            if (context.applicationContext !is Application) {
                return Result.failure()
            }

            withContext(Dispatchers.IO) {
                Intent(context, NtfySmsReceiverService::class.java).also {
                    ContextCompat.startForegroundService(context, it)
                }
            }

            return Result.success()
        }
    }
}
