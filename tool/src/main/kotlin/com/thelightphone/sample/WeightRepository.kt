package com.thelightphone.sample

data class LoggedWeight(
    val id: Long,
    val timestampMillis: Long,
    val weightLbs: Double,
)

class WeightRepository private constructor(
    database: AppDatabase,
) {
    private val dao = database.weightDao()

    fun logWeight(timestampMillis: Long, weightLbs: Double): Long {
        return dao.insert(WeightEntry(timestampMillis = timestampMillis, weightLbs = weightLbs))
    }

    fun getLatestWeight(): LoggedWeight? = dao.getLatest()?.toLoggedWeight()

    fun getAllWeights(): List<LoggedWeight> = dao.getAll().map { it.toLoggedWeight() }

    fun getWeightById(id: Long): LoggedWeight? = dao.getById(id)?.toLoggedWeight()

    fun updateWeight(id: Long, timestampMillis: Long, weightLbs: Double) {
        dao.update(id, timestampMillis, weightLbs)
    }

    fun deleteWeight(id: Long) {
        dao.deleteById(id)
    }

    private fun WeightEntry.toLoggedWeight() = LoggedWeight(
        id = id,
        timestampMillis = timestampMillis,
        weightLbs = weightLbs,
    )

    companion object {
        @Volatile
        private var instance: WeightRepository? = null

        fun getInstance(database: AppDatabase): WeightRepository {
            return instance ?: synchronized(this) {
                instance ?: WeightRepository(database).also { instance = it }
            }
        }
    }
}
