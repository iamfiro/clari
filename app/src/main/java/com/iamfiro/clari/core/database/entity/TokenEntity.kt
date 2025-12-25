package com.iamfiro.clari.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token")
data class TokenEntity(
    @PrimaryKey val id: Int = 1,
    val idToken: String
)

