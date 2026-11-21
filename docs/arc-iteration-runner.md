# Native ARC iteration runner

This fork includes `info.openrocket.core.optimization.arc.ArcIterationRunner`.
It repeatedly runs OpenRocket's own `Simulation` and `FlightData` classes rather
than duplicating the flight model in a separate script.

The runner is generic: an ARC team supplies a candidate factory and a mutation
function that changes rocket geometry, component masses, motor choice, or
simulation conditions through existing OpenRocket APIs. The returned candidates
include native `FlightData`, score, feasibility, and generation number.

Example shape:

```java
var limits = new ArcIterationRunner.Constraints(259.0, 12.0, 230.0, 1.0, 0.35);
var finalists = ArcIterationRunner.search(
    index -> makeCandidateFromYourRocketTemplate(index),
    simulation -> mutateRocketAndReturnIndependentCopy(simulation),
    limits, 250, 120, 7L,
    data -> 0.0,
    simulation -> stabilityAndMassPenalty(simulation, limits));
```

`Simulation.simulate()` is called for every candidate, so results use
OpenRocket's aerodynamics, motor data, recovery events, weather settings, and
flight-data calculations. Use `FlightData.getMaxAltitude()`,
`getMaxVelocity()`, and `getTimeToApogee()` for reporting. The final function
is where a team should enforce geometry-specific stability and mass limits.

The target values above are examples only. Configure them from the current ARC
handbook and verify finalists in the GUI, including stability, launch-rod
clearance, recovery deployment, structural margins, weather, and exact motor.
This is a design-search aid, not a flight-safety certification tool.
