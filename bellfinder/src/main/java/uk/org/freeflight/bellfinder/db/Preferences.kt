package uk.org.freeflight.bellfinder.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "preferences")
data class Preferences(
    @PrimaryKey val idx: Long,
    val unringable: Boolean,
    val bells: String
)