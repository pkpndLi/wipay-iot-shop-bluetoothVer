package com.example.wipay_iot_shop.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class FlagReverseEntity(
    @PrimaryKey(autoGenerate = true)
    @NotNull
    val _id: Int?,

    @ColumnInfo(name = "flag")
    val flagReverse: Boolean?
)