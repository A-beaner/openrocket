# Native ARC iteration runner

This fork now includes `info.openrocket.core.optimization.arc.ArcIterationRunner`.
It repeatedly runs OpenRocket's own `Simulation` and `FlightData` classes instead
of duplicating the flight model in a separate script.

The runner is intentionally generic: an ARC team supplies a candidate factory
and a mutation function that changes the rocket geometry, component masses,
motor choice, or simulation conditions using the existing OpenRocket APIs.
The returned candidates include the native `FlightData`, score, feasibility,
and generation number.

Conceptual usage:

```java
var limits = new ArcIterationRunner.Constraints(
    259.0, 12.0, 230.0, 1.0, 0.35);

var finalists = ArcIterationRunner.search(
    index -> makeCandidateFromYourRocketTemplate(index),
    simulation -> mutateRocketAndReturnIndependentCopy(simulation),
    limits,
    250,
    120,
    7L,
    data -> 0.0);
```

`Simulation.simulate()` is called for every candidate, so finalist results use
OpenRocket's aerodynamics, motor data, recovery events, weather settings, and
flight-data calculations. `data.getMaxAltitude()`, `getMaxVelocity()`, and
`getTimeToApogee()` are available for reporting.

The target values above are examples only. Configure them from the current ARC
handbook and verify finalists in the GUI, including stability, launch-rod
clearance, recovery deployment, structural margins, weather, and exact motor.
This is a design-search aid, not a flight-safety certification tool.
