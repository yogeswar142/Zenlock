package com.zenlock.focusguard.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.zenlock.focusguard.data.db.dao.BlockedAppDao;
import com.zenlock.focusguard.data.db.dao.BlockedAppDao_Impl;
import com.zenlock.focusguard.data.db.dao.FocusSessionDao;
import com.zenlock.focusguard.data.db.dao.FocusSessionDao_Impl;
import com.zenlock.focusguard.data.db.dao.KeywordDao;
import com.zenlock.focusguard.data.db.dao.KeywordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FocusGuardDatabase_Impl extends FocusGuardDatabase {
  private volatile BlockedAppDao _blockedAppDao;

  private volatile FocusSessionDao _focusSessionDao;

  private volatile KeywordDao _keywordDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `blocked_apps` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `isBlocked` INTEGER NOT NULL, `addedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`packageName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `plannedDurationMinutes` INTEGER NOT NULL, `actualDurationSeconds` INTEGER NOT NULL, `sessionType` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `blockedAttempts` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `keywords` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `keyword` TEXT NOT NULL, `type` TEXT NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '33cab21b09a34d4b60790a527fe00b44')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `blocked_apps`");
        db.execSQL("DROP TABLE IF EXISTS `focus_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `keywords`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBlockedApps = new HashMap<String, TableInfo.Column>(4);
        _columnsBlockedApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("isBlocked", new TableInfo.Column("isBlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedApps.put("addedTimestamp", new TableInfo.Column("addedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockedApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockedApps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockedApps = new TableInfo("blocked_apps", _columnsBlockedApps, _foreignKeysBlockedApps, _indicesBlockedApps);
        final TableInfo _existingBlockedApps = TableInfo.read(db, "blocked_apps");
        if (!_infoBlockedApps.equals(_existingBlockedApps)) {
          return new RoomOpenHelper.ValidationResult(false, "blocked_apps(com.zenlock.focusguard.data.db.entity.BlockedAppEntity).\n"
                  + " Expected:\n" + _infoBlockedApps + "\n"
                  + " Found:\n" + _existingBlockedApps);
        }
        final HashMap<String, TableInfo.Column> _columnsFocusSessions = new HashMap<String, TableInfo.Column>(8);
        _columnsFocusSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("plannedDurationMinutes", new TableInfo.Column("plannedDurationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("actualDurationSeconds", new TableInfo.Column("actualDurationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("sessionType", new TableInfo.Column("sessionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("blockedAttempts", new TableInfo.Column("blockedAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFocusSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFocusSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFocusSessions = new TableInfo("focus_sessions", _columnsFocusSessions, _foreignKeysFocusSessions, _indicesFocusSessions);
        final TableInfo _existingFocusSessions = TableInfo.read(db, "focus_sessions");
        if (!_infoFocusSessions.equals(_existingFocusSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "focus_sessions(com.zenlock.focusguard.data.db.entity.FocusSessionEntity).\n"
                  + " Expected:\n" + _infoFocusSessions + "\n"
                  + " Found:\n" + _existingFocusSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsKeywords = new HashMap<String, TableInfo.Column>(4);
        _columnsKeywords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywords.put("keyword", new TableInfo.Column("keyword", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywords.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKeywords.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKeywords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKeywords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoKeywords = new TableInfo("keywords", _columnsKeywords, _foreignKeysKeywords, _indicesKeywords);
        final TableInfo _existingKeywords = TableInfo.read(db, "keywords");
        if (!_infoKeywords.equals(_existingKeywords)) {
          return new RoomOpenHelper.ValidationResult(false, "keywords(com.zenlock.focusguard.data.db.entity.KeywordEntity).\n"
                  + " Expected:\n" + _infoKeywords + "\n"
                  + " Found:\n" + _existingKeywords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "33cab21b09a34d4b60790a527fe00b44", "3b8f618f9c1a1952814084d12fa691d3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "blocked_apps","focus_sessions","keywords");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `blocked_apps`");
      _db.execSQL("DELETE FROM `focus_sessions`");
      _db.execSQL("DELETE FROM `keywords`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BlockedAppDao.class, BlockedAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FocusSessionDao.class, FocusSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(KeywordDao.class, KeywordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BlockedAppDao blockedAppDao() {
    if (_blockedAppDao != null) {
      return _blockedAppDao;
    } else {
      synchronized(this) {
        if(_blockedAppDao == null) {
          _blockedAppDao = new BlockedAppDao_Impl(this);
        }
        return _blockedAppDao;
      }
    }
  }

  @Override
  public FocusSessionDao focusSessionDao() {
    if (_focusSessionDao != null) {
      return _focusSessionDao;
    } else {
      synchronized(this) {
        if(_focusSessionDao == null) {
          _focusSessionDao = new FocusSessionDao_Impl(this);
        }
        return _focusSessionDao;
      }
    }
  }

  @Override
  public KeywordDao keywordDao() {
    if (_keywordDao != null) {
      return _keywordDao;
    } else {
      synchronized(this) {
        if(_keywordDao == null) {
          _keywordDao = new KeywordDao_Impl(this);
        }
        return _keywordDao;
      }
    }
  }
}
