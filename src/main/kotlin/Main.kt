import java.io.File
import java.util.SortedSet

data class Station(
    val id: Int,
    val cargoUnloadType: Int,
    val cargoLoadType: Int,
    val neighbours: MutableSet<Station> = mutableSetOf(),
) : Comparable<Station> {
    override fun compareTo(other: Station) = id.compareTo(other.id)

    /*
     Needed to prevent stack overflow when calculating hashCode and equals of stations which are a part of a cycle,
     which will trigger the calculation of hashCode and equals of neighbours, which will eventually cycle back to the
     original station
     */
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = other is Station && id == other.id
}

// Represents departure from station, with cargo post departure from that station
data class StationDeparture(val station: Station, val cargo: Set<Int>)

fun computeCargoPostDeparture(arrivalCargo: Set<Int>, station: Station): Set<Int> =
    (arrivalCargo - station.cargoUnloadType) + station.cargoLoadType // Unload, then load

fun computePossibleCargoForEachStation(startingStation: Station): Map<Station, Set<Int>> {
    // Keeps track of seen departures, to avoid duplicate cycle computation
    val departures = mutableSetOf<StationDeparture>()

    // Keeps track of departures yet to be computed, discarding the already computed ones
    val computeQueue = ArrayDeque<StationDeparture>()

    val initialCargo = computeCargoPostDeparture(emptySet(), startingStation)
    val initialDeparture = StationDeparture(startingStation, initialCargo)
    departures.add(initialDeparture)
    computeQueue.add(initialDeparture)

    val possibleCargoForEachStation = mutableMapOf<Station, MutableSet<Int>>()
    while (computeQueue.isNotEmpty()) {
        val (currentStation, currentCargo) = computeQueue.removeFirst()
        for (neighbor in currentStation.neighbours) {
            /*
             Add the current departure cargo types to the set of possible cargo types upon arrival to all the
             neighbouring stations, because departure cargo from the from station is equal to the arrival cargo of the
             to station
             */
            possibleCargoForEachStation.getOrPut(neighbor) { mutableSetOf() }.addAll(currentCargo)

            val cargoPostDeparture = computeCargoPostDeparture(currentCargo, neighbor)
            val departure = StationDeparture(neighbor, cargoPostDeparture)
            if (departures.add(departure)) { // Add departure to the compute queue, only if it hasn't already been seen
                computeQueue.add(departure)
            }
        }
    }

    return possibleCargoForEachStation
}

fun readInts(bufferedReader: java.io.BufferedReader, expectedCount: Int, context: String): List<Int> {
    val line = bufferedReader.readLine()
        ?: throw IllegalArgumentException("Unexpected end of input while reading $context")

    val parts = line.trim().split("\\s+".toRegex()) // Split by one or more whitespaces
    require(parts.size == expectedCount) { "Expected $expectedCount values for $context but got ${parts.size}" }

    return parts.map {
        token -> token.toIntOrNull() ?: throw IllegalArgumentException("Expected integer for $context but got $token")
    }
}

fun parseInput(input: File): Pair<SortedSet<Station>, Station> {
    val bufferedReader = input.bufferedReader()

    val (s, t) = readInts(bufferedReader, 2, "station and track counts")
    require(s >= 0) { "Station count must be non-negative" }
    require(t >= 0) { "Track count must be non-negative" }

    val stations = mutableMapOf<Int, Station>()
    repeat(s) {
        val (id, cargoUnloadType, cargoLoadType) =
            readInts(bufferedReader, 3, "station declaration")
        require(!stations.containsKey(id))  { "Duplicate station id: $id" }
        stations[id] = Station(id, cargoUnloadType, cargoLoadType)
    }

    repeat(t) {
        val (fromId, toId) = readInts(bufferedReader, 2, "track declaration")
        require(stations.containsKey(fromId)) { "Track references non-existent from station with id $fromId" }
        require(stations.containsKey(toId)) { "Track references non-existent to station with id $toId" }
        stations[fromId]!!.neighbours.add(stations[toId]!!)
    }

    val (startId) = readInts(bufferedReader, 1, "starting station")
    require(stations.containsKey(startId)) {
        "Starting station references non-existent station with id $startId"
    }

    return Pair(stations.values.toSortedSet(), stations[startId]!!)
}

fun main(args: Array<String>) {
    try {
        require(args.isNotEmpty()) { "Path to input not provided" }

        val file = File(args[0])
        require(file.exists()) { "File at path ${args[0]} not found" }

        val (stations, startingStation) = parseInput(file)
        val possibleCargoForEachStation = computePossibleCargoForEachStation(startingStation)

        val stringBuilder = StringBuilder()
        for (station in stations) {
            val cargo = possibleCargoForEachStation[station] ?: emptySet()
            stringBuilder.appendLine("Station ${station.id}: $cargo")
        }

        print(stringBuilder)
    } catch (e: IllegalArgumentException) {
        println(e.message)
    }
}