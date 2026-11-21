# Native ARC iteration runner

This fork includes `info.openrocket.core.optimization.arc.ArcIterationRunner`.
It repeatedly runs OpenRocket's own `Simulation` and `FlightData` classes rather
than duplicating the flight model in a separate script.

`ArcRunLedger` appends ranked candidates to `arc-runs.csv` (or any path supplied
to it). Every search invocation gets a run ID, and each archived candidate is
stored with its rank, generation, feasibility, score, apogee, maximum velocity,
and time to apogee. This makes runs comparable instead of overwriting previous
results.

Example shape:

```java
var finalists = ArcIterationRunner.search(
    index -> makeCandidateFromYourRocketTemplate(index),
    simulation -> mutateRocketAndReturnIndependentCopy(simulation),
    new ArcIterationRunner.Constraints(259.0, 12.0, 230.0, 1.0, 0.35),
    250, 120, 7L, data -> 0.0,
    simulation -> stabilityAndMassPenalty(simulation));
ArcRunLedger.append(Path.of("arc-runs.csv"), finalists);
```

The final penalty function is where a team should enforce geometry-specific
stability and mass limits. Configure targets from the current ARC handbook and
verify finalists in the GUI, including stability, launch-rod clearance,
recovery deployment, structural margins, weather, and exact motor.

This is a design-search aid, not a flight-safety certification tool.
