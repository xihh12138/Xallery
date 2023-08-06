package com.xallery.common.repository.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.xallery.common.repository.db.model.Source

@Dao
interface SourceDao {

    //<editor-fold desc="增增增增增增增增增增增增增增增增增增增增增增增增增增增增增增">

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(sourceList: List<Source>)

    //</editor-fold>增增增增增增增增增增增增增增增增增增增增增增增增增增增增增增


    //<editor-fold desc="删删删删删删删删删删删删删删删删删删删删删删删删删删删删删删">

    @Query("DELETE FROM Source")
    suspend fun deleteAll()

    //</editor-fold>删删删删删删删删删删删删删删删删删删删删删删删删删删删删删删


    //<editor-fold desc="改改改改改改改改改改改改改改改改改改改改改改改改改改改改改改">

    //</editor-fold>改改改改改改改改改改改改改改改改改改改改改改改改改改改改改改


    //<editor-fold desc="查查查查查查查查查查查查查查查查查查查查查查查查查查查查查查">

    @RawQuery(observedEntities = [Source::class])
    suspend fun query(sortBy: SupportSQLiteQuery): List<Source>

    @Query("SELECT COUNT(id) FROM SOURCE")
    suspend fun getCount(): Int

    //</editor-fold>查查查查查查查查查查查查查查查查查查查查查查查查查查查查查查

}