package info.openrocket.core.optimization.arc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Appends ranked ARC candidates to a durable CSV run ledger. */
public final class ArcRunLedger {
    private static final String HEADER = "timestamp,run_id,rank,generation,feasible,score,apogee_m,max_velocity_mps,time_to_apogee_s\n";

    private ArcRunLedger() {
    }

    /**
     * Append a ranked archive to {@code path}. One search invocation gets one
     * run ID; rows are written in their supplied rank order.
     */
    public static void append(Path path, List<ArcIterationRunner.Candidate> rankedCandidates)
            throws IOException {
        boolean newFile = Files.notExists(path) || Files.size(path) == 0;
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        String runId = UUID.randomUUID().toString();
        StringBuilder csv = new StringBuilder();
        if (newFile) {
            csv.append(HEADER);
        }
        String timestamp = Instant.now().toString();
        for (int i = 0; i < rankedCandidates.size(); i++) {
            ArcIterationRunner.Candidate candidate = rankedCandidates.get(i);
            var data = candidate.flightData();
            csv.append(timestamp).append(',')
                    .append(runId).append(',').append(i + 1).append(',')
                    .append(candidate.generation()).append(',')
                    .append(candidate.feasible()).append(',')
                    .append(candidate.score()).append(',')
                    .append(data.getMaxAltitude()).append(',')
                    .append(data.getMaxVelocity()).append(',')
                    .append(data.getTimeToApogee()).append('\n');
        }
        Files.writeString(path, csv.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
