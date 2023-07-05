package com.xallery.common.repository.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xallery.common.repository.db.converter.StringListConverter
import com.xallery.common.repository.db.dao.SourceDao
import com.xallery.common.repository.db.model.Source
import com.xihh.base.android.appContext

@Database(
    entities = [
        Source::class
    ],
    version = 1
)
@TypeConverters(StringListConverter::class)
abstract class XalleryDB : RoomDatabase() {

    abstract fun sourceDao(): SourceDao

    companion object {

        val sourceDao get() = instance.sourceDao()

        private const val DB_NAME = "xallery_android.db"

        val instance: XalleryDB by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            Room.databaseBuilder(appContext, XalleryDB::class.java, DB_NAME)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {

                    }
                })
                .build()
        }

    }
}

val db get() = XalleryDB.Companion