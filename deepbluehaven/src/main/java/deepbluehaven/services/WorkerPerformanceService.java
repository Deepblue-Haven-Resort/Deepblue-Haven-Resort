package deepbluehaven.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.WorkerProfile;
import deepbluehaven.repositories.WorkerProfileRepository;

@Service
public class WorkerPerformanceService {

    private final WorkerProfileRepository workerProfileRepository;

    public WorkerPerformanceService(WorkerProfileRepository workerProfileRepository) {
        this.workerProfileRepository = workerProfileRepository;
    }

    @Transactional
    public void adjustWorkerPerformanceScore(Worker worker, double delta) {
        if (worker == null) return;

        WorkerProfile profile = worker.getProfile();
        if (profile == null) {
            profile = workerProfileRepository.findByWorkerId(worker.getId()).orElse(null);
            if (profile == null) return;
        }

        double currentScore = profile.getPerformanceScore() != null ? profile.getPerformanceScore() : 90.0;
        double newScore = Math.max(0.0, Math.min(100.0, currentScore + delta));

        profile.setPerformanceScore(newScore);
        workerProfileRepository.save(profile);
    }
}
