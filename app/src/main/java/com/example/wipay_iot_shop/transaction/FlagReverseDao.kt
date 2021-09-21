package com.example.wipay_iot_shop.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.testpos.database.transaction.ReversalEntity

@Dao
interface FlagReverseDao {

    @Query("SELECT * FROM FlagReverseEntity ORDER BY _id DESC LIMIT 1")
    fun getFlagReverse(): FlagReverseEntity

    @Insert
    fun insertFlagReverse(flagReverseEntity: FlagReverseEntity)

    @Update
    fun updateFlagReverse(flagReverseEntity: FlagReverseEntity)

}