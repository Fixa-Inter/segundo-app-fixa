package com.aula.fixa.infrastructure.local;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public final class OfflineSolicitacaoSyncScheduler {
    private static final String UNIQUE_WORK = "fixa-offline-solicitacoes-sync";
    private OfflineSolicitacaoSyncScheduler() { }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(OfflineSolicitacaoSyncWorker.class)
                .setConstraints(constraints).build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }
}
