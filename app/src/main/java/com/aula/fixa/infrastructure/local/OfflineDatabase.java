package com.aula.fixa.infrastructure.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistência local de respostas HTTP para fallback e solicitações pendentes.
 */
public final class OfflineDatabase extends SQLiteOpenHelper {
    private static final String NAME = "fixa_offline.db";
    private static final int VERSION = 1;
    private static final long CACHE_RETENTION_MS = 60L * 24 * 60 * 60 * 1000;
    private static final long MAX_CACHE_BYTES = 50L * 1024 * 1024;
    private static volatile OfflineDatabase instance;

    public OfflineDatabase(Context context) {
        super(context.getApplicationContext(), NAME, null, VERSION);
    }

    public static OfflineDatabase get(Context context) {
        OfflineDatabase current = instance;
        if (current == null) {
            synchronized (OfflineDatabase.class) {
                current = instance;
                if (current == null) {
                    instance = current = new OfflineDatabase(context);
                }
            }
        }
        return current;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE api_cache (cache_key TEXT NOT NULL, owner_key TEXT NOT NULL, payload BLOB NOT NULL, content_type TEXT, cached_at INTEGER NOT NULL, PRIMARY KEY(cache_key, owner_key))");
        db.execSQL("CREATE INDEX api_cache_owner_date ON api_cache(owner_key, cached_at DESC)");
        db.execSQL("CREATE TABLE pending_requests (id TEXT PRIMARY KEY NOT NULL, owner_key TEXT NOT NULL, payload BLOB NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX pending_requests_owner_date ON pending_requests(owner_key, created_at ASC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized CachedResponse get(String key, String owner) {
        long now = System.currentTimeMillis();

        try (Cursor cursor = getReadableDatabase().query(
                "api_cache",
                new String[]{"payload", "content_type", "cached_at"},
                "cache_key=? AND owner_key=? AND cached_at>=?",
                new String[]{key, owner, String.valueOf(now - CACHE_RETENTION_MS)},
                null, null, null, "1"
        )) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            return new CachedResponse(
                    cursor.getBlob(0),
                    cursor.isNull(1)
                            ? null
                            : cursor.getString(1),
                    cursor.getLong(2)
            );
        }
    }

    public synchronized void put(String key, String owner, byte[] payload, String contentType) {
        if (payload == null || payload.length == 0) {
            return;
        }

        ContentValues values = new ContentValues();

        values.put("cache_key", key);
        values.put("owner_key", owner);
        values.put("payload", payload);
        values.put("content_type", contentType);
        values.put("cached_at", System.currentTimeMillis());

        SQLiteDatabase db = getWritableDatabase();
        db.insertWithOnConflict(
                "api_cache",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.delete(
                "api_cache",
                "cached_at<?",
                new String[]{String.valueOf(System.currentTimeMillis() - CACHE_RETENTION_MS)}
        );

        db.execSQL("DELETE FROM api_cache WHERE rowid NOT IN (SELECT rowid FROM api_cache ORDER BY cached_at DESC LIMIT 300)");
        trimCache(db);
    }

    private static void trimCache(SQLiteDatabase db) {
        while (true) {
            long size = 0;
            try (Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(LENGTH(payload)),0) FROM api_cache", null)) {
                if (cursor.moveToFirst()) {
                    size = cursor.getLong(0);
                }
            }

            if (size <= MAX_CACHE_BYTES) {
                return;
            }

            db.execSQL("DELETE FROM api_cache WHERE rowid=(SELECT rowid FROM api_cache ORDER BY cached_at ASC LIMIT 1)");
        }
    }

    public synchronized void enqueueRequest(String id, String owner, byte[] payload) {
        ContentValues values = new ContentValues();

        values.put("id", id);
        values.put("owner_key", owner);
        values.put("payload", payload);
        values.put("created_at", System.currentTimeMillis());

        getWritableDatabase().insertWithOnConflict(
                "pending_requests",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public synchronized List<PendingRequest> pendingRequests(String owner) {
        List<PendingRequest> pending = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                "pending_requests",
                new String[]{"id", "payload", "created_at"},
                "owner_key=?",
                new String[]{owner},
                null,
                null,
                "created_at ASC"
        )) {
            while (cursor.moveToNext()) {
                pending.add(new PendingRequest(
                        cursor.getString(0),
                        cursor.getBlob(1),
                        cursor.getLong(2)
                ));
            }
        }

        return pending;
    }

    public synchronized void deletePendingRequest(String id) {
        getWritableDatabase().delete(
                "pending_requests",
                "id=?",
                new String[]{id}
        );
    }

    public static final class CachedResponse {
        public final byte[] payload;
        public final String contentType;
        public final long cachedAt;

        CachedResponse(byte[] payload, String contentType, long cachedAt) {
            this.payload = payload;
            this.contentType = contentType;
            this.cachedAt = cachedAt;
        }
    }

    public static final class PendingRequest {
        public final String id;
        public final byte[] payload;
        public final long createdAt;

        PendingRequest(String id, byte[] payload, long createdAt) {
            this.id = id;
            this.payload = payload;
            this.createdAt = createdAt;
        }
    }
}
