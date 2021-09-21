package com.example.wipay_iot_shop.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface StuckReverseDao {

    @Query("SELECT * FROM StuckReverseEntity ORDER BY _id DESC LIMIT 1")
    fun getStuckReverse(): StuckReverseEntity

    @Insert
    fun insertStuckReverse(stuckReverseEntity: StuckReverseEntity)

    @Update
    fun updateStuckReverse(stuckReverseEntity: StuckReverseEntity)
}