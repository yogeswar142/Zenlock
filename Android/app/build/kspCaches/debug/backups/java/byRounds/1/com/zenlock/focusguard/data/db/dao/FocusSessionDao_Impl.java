package com.zenlock.focusguard.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.zenlock.focusguard.data.db.entity.FocusSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class FocusSessionDao_Impl implements FocusSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FocusSessionEntity> __insertionAdapterOfFocusSessionEntity;

  private final EntityDeletionOrUpdateAdapter<FocusSessionEntity> __updateAdapterOfFocusSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSessions;

  public FocusSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFocusSessionEntity = new EntityInsertionAdapter<FocusSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `focus_sessions` (`id`,`startTime`,`endTime`,`plannedDurationMinutes`,`actualDurationSeconds`,`sessionType`,`isCompleted`,`blockedAttempts`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FocusSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTime());
        }
        statement.bindLong(4, entity.getPlannedDurationMinutes());
        statement.bindLong(5, entity.getActualDurationSeconds());
        statement.bindString(6, entity.getSessionType());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getBlockedAttempts());
      }
    };
    this.__updateAdapterOfFocusSessionEntity = new EntityDeletionOrUpdateAdapter<FocusSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `focus_sessions` SET `id` = ?,`startTime` = ?,`endTime` = ?,`plannedDurationMinutes` = ?,`actualDurationSeconds` = ?,`sessionType` = ?,`isCompleted` = ?,`blockedAttempts` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FocusSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTime());
        }
        statement.bindLong(4, entity.getPlannedDurationMinutes());
        statement.bindLong(5, entity.getActualDurationSeconds());
        statement.bindString(6, entity.getSessionType());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getBlockedAttempts());
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllSessions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM focus_sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final FocusSessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFocusSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final FocusSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFocusSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSessions(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSessions.acquire();
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
          __preparedStmtOfDeleteAllSessions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FocusSessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM focus_sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _item = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
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
  public Flow<List<FocusSessionEntity>> getCompletedSessions() {
    final String _sql = "SELECT * FROM focus_sessions WHERE isCompleted = 1 ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _item = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
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
  public Flow<List<FocusSessionEntity>> getSessionsSince(final long startTime) {
    final String _sql = "SELECT * FROM focus_sessions WHERE startTime >= ? ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _item = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
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
  public Object getCompletedSessionsSince(final long startTime,
      final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE startTime >= ? AND isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _item = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
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
  public Object getTotalFocusTimeSince(final long startTime,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT SUM(actualDurationSeconds) FROM focus_sessions WHERE isCompleted = 1 AND startTime >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Object getCompletedSessionCountSince(final long startTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM focus_sessions WHERE isCompleted = 1 AND startTime >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
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

  @Override
  public Object getTotalBlockedAttemptsSince(final long startTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(blockedAttempts) FROM focus_sessions WHERE startTime >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Object getSessionById(final long id,
      final Continuation<? super FocusSessionEntity> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FocusSessionEntity>() {
      @Override
      @Nullable
      public FocusSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final FocusSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _result = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
          } else {
            _result = null;
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
  public Object getActiveSession(final Continuation<? super FocusSessionEntity> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FocusSessionEntity>() {
      @Override
      @Nullable
      public FocusSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfPlannedDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDurationMinutes");
          final int _cursorIndexOfActualDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDurationSeconds");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfBlockedAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedAttempts");
          final FocusSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final int _tmpPlannedDurationMinutes;
            _tmpPlannedDurationMinutes = _cursor.getInt(_cursorIndexOfPlannedDurationMinutes);
            final long _tmpActualDurationSeconds;
            _tmpActualDurationSeconds = _cursor.getLong(_cursorIndexOfActualDurationSeconds);
            final String _tmpSessionType;
            _tmpSessionType = _cursor.getString(_cursorIndexOfSessionType);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final int _tmpBlockedAttempts;
            _tmpBlockedAttempts = _cursor.getInt(_cursorIndexOfBlockedAttempts);
            _result = new FocusSessionEntity(_tmpId,_tmpStartTime,_tmpEndTime,_tmpPlannedDurationMinutes,_tmpActualDurationSeconds,_tmpSessionType,_tmpIsCompleted,_tmpBlockedAttempts);
          } else {
            _result = null;
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
