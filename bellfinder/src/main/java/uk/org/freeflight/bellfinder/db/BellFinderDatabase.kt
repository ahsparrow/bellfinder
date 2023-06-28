/*
Bell Finder - A directory of English style bell towers

Copyright (C) 2020  Alan Sparrow

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package uk.org.freeflight.bellfinder.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Tower::class, Visit::class, Preferences::class],
    views = [VisitView::class],
    version = 5,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class BellFinderDatabase : RoomDatabase() {
    abstract fun bellFinderDao(): BellFinderDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: BellFinderDatabase? = null

        fun getDatabase(context: Context): BellFinderDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BellFinderDatabase::class.java,
                    "tower_database")
                    .addMigrations(MIGRATION1TO2(), MIGRATION2TO3(), MIGRATION3TO4(), MIGRATION4TO5())
                    .addCallback(RoomCallback())
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    class RoomCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO Preferences (idx, unringable, bells) VALUES (1, 0, '345680T')")
        }
    }

    class MIGRATION4TO5 : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE `Preferences` (" +
                        "idx INTEGER NOT NULL, " +
                        "unringable INTEGER NOT NULL, " +
                        "bells TEXT NOT NULL, " +
                        "PRIMARY KEY(idx))"
            )

            database.execSQL("INSERT INTO `Preferences` (idx, unringable, bells) VALUES (1, 0, '345680T')")
        }
    }

    class MIGRATION3TO4 : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Can't drop individual columns, so drop and recreate the entire towers table
            database.execSQL("DROP TABLE `Towers`")
            database.execSQL(
                "CREATE TABLE `Towers` (" +
                        "towerId INTEGER NOT NULL, " +
                        "place TEXT NOT NULL, " +
                        "county TEXT NOT NULL, " +
                        "dedication TEXT NOT NULL, " +
                        "bells INTEGER NOT NULL, " +
                        "weight INTEGER NOT NULL, " +
                        "unringable INTEGER NOT NULL, " +
                        "practice TEXT NOT NULL, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "PRIMARY KEY(towerId))"
            )

            // Drop and recreate the VisitView view
            database.execSQL("DROP VIEW `VisitView`")
            database.execSQL("CREATE VIEW `VisitView` AS SELECT " +
                    "visits.visitId, visits.towerId, visits.date, visits.notes, " +
                    "visits.peal, visits.quarter, " +
                    "towers.place AS place, " +
                    "towers.county AS county, towers.bells AS bells " +
                    "FROM visits INNER JOIN towers ON visits.towerId = towers.towerId"
            )
        }
    }

    class MIGRATION2TO3 : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Can't drop individual columns, so drop and recreate the entire towers table
            database.execSQL("DROP TABLE `Towers`")
            database.execSQL(
                "CREATE TABLE `Towers` (" +
                        "towerId INTEGER NOT NULL, " +
                        "place TEXT NOT NULL, " +
                        "placeCountyList TEXT, " +
                        "county TEXT, " +
                        "dedication TEXT, " +
                        "bells INTEGER NOT NULL, " +
                        "weight INTEGER NOT NULL, " +
                        "unringable INTEGER NOT NULL, " +
                        "practice TEXT, " +
                        "latitude REAL NOT NULL, " +
                        "longitude REAL NOT NULL, " +
                        "PRIMARY KEY(towerId))"
            )
        }
    }

    class MIGRATION1TO2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Can't drop individual columns, so drop and recreate the entire towers table
            database.execSQL("DROP TABLE `Towers`")
            database.execSQL("CREATE TABLE `Towers` (" +
                    "towerId INTEGER NOT NULL, " +
                    "place TEXT NOT NULL, " +
                    "placeCountyList TEXT, " +
                    "county TEXT, " +
                    "dedication TEXT, " +
                    "bells INTEGER NOT NULL, " +
                    "weight INTEGER NOT NULL, " +
                    "unringable INTEGER NOT NULL, " +
                    "practiceNight TEXT, " +
                    "practiceExtra TEXT, " +
                    "latitude REAL NOT NULL, " +
                    "longitude REAL NOT NULL, " +
                    "PRIMARY KEY(towerId))"
            )

            // Drop and recreate the VisitView view
            database.execSQL("DROP VIEW `VisitView`")
            database.execSQL("CREATE VIEW `VisitView` AS SELECT " +
                    "visits.visitId, visits.towerId, visits.date, visits.notes, " +
                    "visits.peal, visits.quarter, " +
                    "towers.place AS place, towers.placeCountyList AS placeCountyList, " +
                    "towers.county AS county, towers.bells AS bells " +
                    "FROM visits INNER JOIN towers ON visits.towerId = towers.towerId"
            )

            // Translate TowerId from old internal ID to TowerBase
            val cursor = database.query("SELECT VisitId, TowerId FROM Visits")
            val visits = generateSequence { if (cursor.moveToNext()) cursor else null }
                .map { Pair(it.getInt(0), it.getInt(1)) }

            for (visit in visits) {
                database.update(
                    "Visits",
                    SQLiteDatabase.CONFLICT_IGNORE,
                    ContentValues(1).apply { put("TowerId", TowerBaseIds[visit.second]) },
                    "VisitId = ${visit.first}",
                    null
                )
            }
            
            cursor.close()
        }
    }
}