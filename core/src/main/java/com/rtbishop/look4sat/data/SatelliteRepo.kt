package com.rtbishop.look4sat.data

import com.rtbishop.look4sat.domain.model.SatEntry
import com.rtbishop.look4sat.domain.model.SatItem
import com.rtbishop.look4sat.domain.model.SatTrans
import com.rtbishop.look4sat.domain.predict4kotlin.Satellite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

class SatelliteRepo(
    private val localSource: LocalDataSource,
    private val remoteSource: RemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher
) {

    fun getSatItems(): Flow<List<SatItem>> {
        return localSource.getSatItems()
    }

    fun getTransmittersForSat(catNum: Int): Flow<List<SatTrans>> {
        return localSource.getTransmittersForSat(catNum)
    }

    suspend fun getSelectedSatellites(): List<Satellite> {
        return localSource.getSelectedSatellites()
    }

    suspend fun importDataFromStream(stream: InputStream) = withContext(ioDispatcher) {
        val importedEntries = Satellite.importElements(stream).map { tle -> SatEntry(tle) }
        localSource.updateEntries(importedEntries)
    }

    suspend fun importDataFromWeb(sources: List<String>) {
        coroutineScope {
            launch(ioDispatcher) {
                val importedEntries = mutableListOf<SatEntry>()
                val streams = mutableListOf<InputStream>()
                sources.forEach { source ->
                    remoteSource.fetchDataStream(source)?.let { stream ->
                        if (source.contains(".zip", true)) {
                            val zipStream = ZipInputStream(stream).apply { nextEntry }
                            streams.add(zipStream)
                        } else {
                            streams.add(stream)
                        }
                    }
                }
                streams.forEach { stream ->
                    val entries = Satellite.importElements(stream).map { tle -> SatEntry(tle) }
                    importedEntries.addAll(entries)
                }
                localSource.updateEntries(importedEntries)
            }
            launch(ioDispatcher) {
                val transmitters = remoteSource.fetchTransmitters().filter { it.isAlive }
                localSource.updateTransmitters(transmitters)
            }
        }
    }

    suspend fun updateEntriesSelection(catNums: List<Int>, isSelected: Boolean) {
        localSource.updateEntriesSelection(catNums, isSelected)
    }
}