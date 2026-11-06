package com.aula.fixa.infrastructure.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.cloudinary.CloudinaryUploadService;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.ProblemaCriarRequest;
import com.google.gson.Gson;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public final class OfflineSolicitacaoSyncWorker extends Worker {
    public OfflineSolicitacaoSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull @Override public Result doWork() {
        SessionStorage session = new SessionStorage(getApplicationContext());
        if (!session.hasSession()) return Result.retry();

        OfflineDatabase database = OfflineDatabase.get(getApplicationContext());
        List<OfflineDatabase.PendingRequest> pending = database.pendingRequests(session.localDataOwnerKey());

        Gson gson = new Gson();
        for (OfflineDatabase.PendingRequest row : pending) {
            try {
                String payload = new String(session.decryptLocalData(row.payload), StandardCharsets.UTF_8);
                PendingSolicitacao saved = gson.fromJson(payload, PendingSolicitacao.class);
                ProblemaCriarRequest request = new ProblemaCriarRequest();

                request.titulo = saved.titulo;
                request.descricaoProblema = saved.descricaoProblema;
                request.localUnidadeID = saved.localUnidadeId;
                request.descricaoLocal = saved.descricaoLocal;
                request.categoriaEquipamentoId = saved.categoriaEquipamentoId;
                request.urlsFoto = saved.urlsFoto == null ? new ArrayList<>() : new ArrayList<>(saved.urlsFoto);

                if (saved.fotosLocais != null) {
                    for (String path : saved.fotosLocais) request.urlsFoto.add(CloudinaryUploadService.salvarBloqueante(getApplicationContext(), path));
                }

                Response<?> response = ApiClient.solicitacoes(session).criar(request).execute();
                if (!response.isSuccessful()) {
                    if (response.code() >= 500 || response.code() == 408 || response.code() == 429) return Result.retry();
                    return Result.failure();
                }

                if (response.body() == null) return Result.retry();

                database.deletePendingRequest(row.id);
                if (saved.fotosLocais != null) for (String path : saved.fotosLocais) new File(path).delete();
            } catch (Exception error) {
                return Result.retry();
            }
        }

        return Result.success();
    }
}
