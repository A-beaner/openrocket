package info.openrocket.core.optimization.arc;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.FlightData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/** Runs repeatable population-based searches using OpenRocket's native model. */
public final class ArcIterationRunner {
    private ArcIterationRunner() {
    }

    public record Constraints(double targetAltitudeM, double altitudeToleranceM,
                              double maxVelocityMps, double minStabilityCalibers,
                              double maxMassKg) {
        public Constraints {
            if (targetAltitudeM <= 0 || altitudeToleranceM <= 0 || maxVelocityMps <= 0) {
                throw new IllegalArgumentException("ARC targets and limits must be positive");
            }
        }
    }

    public record Candidate(Simulation simulation, FlightData flightData, double score,
                            boolean feasible, int generation) {
    }

    /**
     * Search for low-score candidates. The factory creates the initial
     * population; mutate must return an independent Simulation copy.
     *
     * <p>The constraintPenalty function should return zero for a candidate
     * satisfying team-specific constraints, and a positive penalty otherwise.
     * It is the hook for checking OpenRocket component mass and stability data,
     * which are not part of FlightData.</p>
     */
    public static List<Candidate> search(Function<Integer, Simulation> factory,
                                         Function<Simulation, Simulation> mutate,
                                         Constraints constraints, int generations,
                                         int populationSize, long randomSeed,
                                         ToDoubleFunction<FlightData> objective,
                                         ToDoubleFunction<Simulation> constraintPenalty) {
        if (generations < 1 || populationSize < 2) {
            throw new IllegalArgumentException("generations and populationSize are too small");
        }
        Random random = new Random(randomSeed);
        List<Simulation> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(factory.apply(i));
        }

        List<Candidate> archive = new ArrayList<>();
        for (int generation = 0; generation < generations; generation++) {
            List<Candidate> ranked = new ArrayList<>();
            for (Simulation simulation : population) {
                simulation.simulate();
                FlightData data = simulation.getSimulatedData();
                if (data == null) {
                    throw new IllegalStateException("OpenRocket returned no FlightData");
                }
                double altitudeError = Math.abs(data.getMaxAltitude() - constraints.targetAltitudeM())
                        / constraints.altitudeToleranceM();
                double speedPenalty = Math.max(0, data.getMaxVelocity() - constraints.maxVelocityMps()) / 20.0;
                double designPenalty = Math.max(0, constraintPenalty.applyAsDouble(simulation));
                double score = altitudeError + speedPenalty + designPenalty + objective.applyAsDouble(data);
                ranked.add(new Candidate(simulation, data, score,
                        altitudeError <= 1 && speedPenalty == 0 && designPenalty == 0, generation));
            }
            ranked.sort(Comparator.comparingDouble(Candidate::score));
            archive.addAll(ranked.subList(0, Math.min(10, ranked.size())));

            int eliteCount = Math.max(2, populationSize / 8);
            List<Simulation> next = new ArrayList<>();
            for (int i = 0; i < eliteCount; i++) {
                next.add(ranked.get(i).simulation().clone());
            }
            while (next.size() < populationSize) {
                Candidate parent = ranked.get(random.nextInt(eliteCount));
                next.add(mutate.apply(parent.simulation().clone()));
            }
            population = next;
        }

        archive.sort(Comparator.comparingDouble(Candidate::score));
        return archive.stream().limit(20).toList();
    }
}
