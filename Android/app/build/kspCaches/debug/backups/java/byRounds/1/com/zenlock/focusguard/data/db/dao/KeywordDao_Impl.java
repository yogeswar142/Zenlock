package com.zenlock.focusguard.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.zenlock.focusguard.data.db.entity.KeywordEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class KeywordDao_Impl implements KeywordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<KeywordEntity> __insertionAdapterOfKeywordEntity;

  private final EntityDeletionOrUpdateAdapter<KeywordEntity> __deletionAdapterOfKeywordEntity;

  private final EntityDeletionOrUpdateAdapter<KeywordEntity> __updateAdapterOfKeywordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllKeywords;

  public KeywordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfKeywordEntity = new EntityInsertionAdapter<KeywordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `keywords` (`id`,`keyword`,`type`,`isActive`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KeywordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        statement.bindString(3, entity.getType());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__deletionAdapterOfKeywordEntity = new EntityDeletionOrUpdateAdapter<KeywordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `keywords` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KeywordEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfKeywordEntity = new EntityDeletionOrUpdateAdapter<KeywordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `keywords` SET `id` = ?,`keyword` = ?,`type` = ?,`isActive` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KeywordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        statement.bindString(3, entity.getType());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllKeywords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM keywords";
        return _query;
      }
    };
  }

  @Override
  public Object insertKeyword(final KeywordEntity keyword,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKeywordEntity.insert(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertKeywords(final List<KeywordEntity> keywords,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKeywordEntity.insert(keywords);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteKeyword(final KeywordEntity keyword,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfKeywordEntity.handle(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateKeyword(final KeywordEntity keyword,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfKeywordEntity.handle(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllKeywords(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllKeywords.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllKeywords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<KeywordEntity>> getAllKeywords() {
    final String _sql = "SELECT * FROM keywords ORDER BY type, keyword ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"keywords"}, new Callable<List<KeywordEntity>>() {
      @Override
      @NonNull
      public List<KeywordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<KeywordEntity> _result = new ArrayList<KeywordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KeywordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new KeywordEntity(_tmpId,_tmpKeyword,_tmpType,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActiveKeywordsByType(final String type,
      final Continuation<? super List<KeywordEntity>> $completion) {
    final String _sql = "SELECT * FROM keywords WHERE type = ? AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KeywordEntity>>() {
      @Override
      @NonNull
      public List<KeywordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<KeywordEntity> _result = new ArrayList<KeywordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KeywordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new KeywordEntity(_tmpId,_tmpKeyword,_tmpType,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllowKeywords(final Continuation<? super List<KeywordEntity>> $completion) {
    final String _sql = "SELECT * FROM keywords WHERE type = 'allow' AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KeywordEntity>>() {
      @Override
      @NonNull
      public List<KeywordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<KeywordEntity> _result = new ArrayList<KeywordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KeywordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new KeywordEntity(_tmpId,_tmpKeyword,_tmpType,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getBlockKeywords(final Continuation<? super List<KeywordEntity>> $completion) {
    final String _sql = "SELECT * FROM keywords WHERE type = 'block' AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KeywordEntity>>() {
      @Override
      @NonNull
      public List<KeywordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<KeywordEntity> _result = new ArrayList<KeywordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KeywordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new KeywordEntity(_tmpId,_tmpKeyword,_tmpType,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getChannelKeywords(final Continuation<? super List<KeywordEntity>> $completion) {
    final String _sql = "SELECT * FROM keywords WHERE type = 'channel' AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KeywordEntity>>() {
      @Override
      @NonNull
      public List<KeywordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<KeywordEntity> _result = new ArrayList<KeywordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KeywordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new KeywordEntity(_tmpId,_tmpKeyword,_tmpType,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getKeywordCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM keywords";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
