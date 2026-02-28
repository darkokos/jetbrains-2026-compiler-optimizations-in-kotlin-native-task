# jetbrains-2026-compiler-optimizations-in-kotlin-native-task

## Solution description

The input is read from a file, then parsed into station objects which contain a list of stations that are the
neighbouring stations to that one. This is the way I represented the one-way tracks.

The algorithm which solves the presented problem takes the starting station, as defined in the input, then creates a
station departure object.

This station departure object contains a reference to the station from which the train just departed, and the cargo as
seen after departing that station.

The algorithm then creates a queue and adds this station departure object to it.

While this queue is populated, it will poll it, then add the post-departure cargo to the possible cargo types list in
all the neighbouring stations of the one recorded in the station departure object. It will then compute the
post-departure cargo and create station departure objects for all the neighbouring stations. If such a station departure
object has not been encountered before, which is tracked by a set of computed departures, it will add the said object to
the queue of station departures to be computed.

Once all the unique station departure objects have been computed, the algorithm has successfully traversed the entire
graph through possible branches, thereby assigning the possible cargo types to all the stations which can be reached
from the starting station.

The reason why this works is that the cargo state of a train, as seen after departing a station, is the same as the
cargo state of the same train upon arriving at all the neighbouring stations of the aforementioned station.

I made the assumption that a cargo type can be encountered at a station if a train that just arrived at it carries the
said cargo type, before doing any unloading or loading of cargo. It has not been explicitly stated otherwise in the
task. Even if a cargo type can be encountered at a station only if it is seen on a train after the unloading step has
been finished at that station, the implementation would only need to change to first unload the cargo type that is
unloaded at that station, then record the cargo types that the train has on at the moment, then load the cargo type that
is loaded at that station.

The reason why it is important to compute station departure objects, instead of doing computations by iterating through
stations, is because data is propagated through the graph via departures. It eliminates the possibility of computing
multiple of the same departures (which happens when a train makes the same cycle multiple times) by tracking the
computed departures, and not enqueuing the current one if it has been seen before. This reduces the algorithm's time
complexity to be dependent only on the number of unique combinations of stations and sets of cargo types after
departing those stations.

In the case of an input which produces a disconnected graph, the algorithm will only assign possible cargo types to the
stations connected to the starting station. This is intended behaviour, as no trains can reach the disconnected
stations.

## Improvements

The improvements I could see being feasible are micro-optimizations in memory allocation.

For example, stations being represented by their id, instead of an entire station object, in station departure objects.
Then again, these station objects are used directly, so the memory needed to store the entire station object makes up
for the time needed to find a station by its id.

## Testing

I provided a number of input files which I used to test the algorithm, at [./inputs](./inputs).