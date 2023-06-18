package com.xallery.common.reposity.db

//@Database(
//    entities = [
//    ],
//    version = 1
//)
//@TypeConverters(StringListConverter::class)
//abstract class XalleryDB : RoomDatabase() {
//
//    companion object {
//
//        private const val DB_NAME = "xallery_android.db"
//
//        val instance: XalleryDB by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
//            Room.databaseBuilder(appContext, XalleryDB::class.java, DB_NAME)
//                .addCallback(object : Callback() {
//                    override fun onOpen(db: SupportSQLiteDatabase) {}
//                })
//                .build()
//        }
//
//    }
//}
//
//val db get() = XalleryDB.instance