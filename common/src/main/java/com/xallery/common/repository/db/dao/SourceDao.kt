package com.xallery.common.repository.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xallery.common.repository.db.model.Source

@Dao
interface SourceDao {

    //<editor-fold desc="增增增增增增增增增增增增增增增增增增增增增增增增增增增增增增">

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(sourceList: List<Source>)

    //</editor-fold>增增增增增增增增增增增增增增增增增增增增增增增增增增增增增增


    //<editor-fold desc="删删删删删删删删删删删删删删删删删删删删删删删删删删删删删删">

    //</editor-fold>删删删删删删删删删删删删删删删删删删删删删删删删删删删删删删


    //<editor-fold desc="改改改改改改改改改改改改改改改改改改改改改改改改改改改改改改">

    //</editor-fold>改改改改改改改改改改改改改改改改改改改改改改改改改改改改改改


    //<editor-fold desc="查查查查查查查查查查查查查查查查查查查查查查查查查查查查查查">

    @Query("SELECT * FROM Source ORDER BY :sortBy")
    suspend fun getAllSort(sortBy: String): List<Source>

    @Query("SELECT * FROM Source ORDER BY :sortBy DESC")
    suspend fun getAllDSort(sortBy: String): List<Source>

    @Query("SELECT * FROM Source ORDER BY :sortBy LIMIT :limit")
    suspend fun getLimitSort(limit: Int, sortBy: String): List<Source>

    @Query("SELECT * FROM Source ORDER BY :sortBy DESC LIMIT :limit")
    suspend fun getLimitDSort(limit: Int, sortBy: String): List<Source>

    @Query("SELECT * FROM Source WHERE mimeType LIKE :mimeType || '%' ORDER BY :sortBy")
    suspend fun getByMimeTypeSort(mimeType: String, sortBy: String): List<Source>

    @Query("SELECT * FROM Source WHERE mimeType LIKE :mimeType || '%' ORDER BY :sortBy DESC")
    suspend fun getByMimeTypeDSort(mimeType: String, sortBy: String): List<Source>

    @Query("SELECT * FROM Source WHERE mimeType LIKE :mimeType || '%' ORDER BY :sortBy LIMIT :limit")
    suspend fun getByMimeTypeLimitSort(mimeType: String, limit: Int, sortBy: String): List<Source>

    @Query("SELECT * FROM Source WHERE mimeType LIKE :mimeType || '%' ORDER BY :sortBy DESC LIMIT :limit")
    suspend fun getByMimeTypeLimitDSort(mimeType: String, limit: Int, sortBy: String): List<Source>

    //</editor-fold>查查查查查查查查查查查查查查查查查查查查查查查查查查查查查查

}