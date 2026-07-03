package com.thelightphone.sample

data class LoggedShot(
    val id: Long,
    val timestampMillis: Long,
    val site: InjectionSite,
)

class ShotRepository private constructor(
    database: AppDatabase,
) {
    private val dao = database.shotDao()

    fun logShot(timestampMillis: Long, site: InjectionSite): Long {
        return dao.insert(ShotEntry(timestampMillis = timestampMillis, site = site.name))
    }

    fun getLatestShot(): LoggedShot? = dao.getLatest()?.toLoggedShot()

    fun getAllShots(): List<LoggedShot> = dao.getAll().map { it.toLoggedShot() }

    fun getShotById(id: Long): LoggedShot? = dao.getById(id)?.toLoggedShot()

    fun updateShot(id: Long, timestampMillis: Long, site: InjectionSite) {
        dao.update(id, timestampMillis, site.name)
    }

    fun deleteShot(id: Long) {
        dao.deleteById(id)
    }

    private fun ShotEntry.toLoggedShot() = LoggedShot(
        id = id,
        timestampMillis = timestampMillis,
        site = InjectionSite.valueOf(site),
    )

    companion object {
        @Volatile
        private var instance: ShotRepository? = null

        fun getInstance(database: AppDatabase): ShotRepository {
            return instance ?: synchronized(this) {
                instance ?: ShotRepository(database).also { instance = it }
            }
        }
    }
}
