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

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Tower::class, Visit::class],
    views = [VisitView::class],
    version = 2,
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
                    .addMigrations(MIGRATION1TO2())
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    class MIGRATION1TO2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Towers ADD COLUMN UR INTEGER DEFAULT 0 NOT NULL")
        }
    }
}