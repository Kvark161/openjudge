package eskimo.backend.rest;

import eskimo.backend.entity.Problem;
import eskimo.backend.entity.Submission;
import eskimo.backend.entity.User;
import eskimo.backend.entity.enums.ContestStatus;
import eskimo.backend.entity.enums.Role;
import eskimo.backend.rest.annotations.AccessLevel;
import eskimo.backend.rest.holder.AuthenticationHolder;
import eskimo.backend.rest.request.SubmitProblemWebRequest;
import eskimo.backend.rest.response.SubmissionResponse;
import eskimo.backend.rest.response.SubmitParametersResponse;
import eskimo.backend.services.ProblemService;
import eskimo.backend.services.ProgrammingLanguageService;
import eskimo.backend.services.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
public class SubmissionController {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);

    private final ProblemService problemService;
    private final SubmissionService submissionService;
    private final ProgrammingLanguageService programmingLanguageService;
    private final AuthenticationHolder authenticationHolder;

    public SubmissionController(ProblemService problemService, SubmissionService submissionService,
                                ProgrammingLanguageService programmingLanguageService,
                                AuthenticationHolder authenticationHolder)
    {
        this.problemService = problemService;
        this.submissionService = submissionService;
        this.programmingLanguageService = programmingLanguageService;
        this.authenticationHolder = authenticationHolder;
    }

    @GetMapping("contest/{id}/submissions")
    @AccessLevel(role = Role.USER, contestStatus = ContestStatus.STARTED)
    public List<SubmissionResponse> getUserContestSubmissions(@PathVariable("id") Long contestId) {
        return submissionService.getUserContestSubmissions(authenticationHolder.getUser().getId(), contestId);
    }

    @PostMapping("contest/submit")
    @AccessLevel(role = Role.USER, contestStatus = ContestStatus.RUNNING)
    public void submitProblem(@RequestBody SubmitProblemWebRequest submitProblemWebRequest) throws IllegalAccessException {
        Role role = authenticationHolder.getUser().getRole();
        Problem problem = problemService.getProblem(submitProblemWebRequest.getContestId(), submitProblemWebRequest.getProblemIndex());
        if (problem.isHidden() && role != Role.ADMIN) {
            throw new IllegalAccessException("User can't submit to hidden problem");
        }
        submissionService.submit(submitProblemWebRequest);
    }

    @GetMapping("submission/{id}/rejudge")
    @AccessLevel(role = Role.ADMIN)
    public void rejudge(@PathVariable("id") Long submissionId) {
        submissionService.rejudge(submissionId);
    }

    @GetMapping("submission/{submissionId}")
    @AccessLevel(role = Role.USER, contestStatus = ContestStatus.STARTED)
    public Submission getSubmission(@PathVariable Long submissionId) {
        User user = authenticationHolder.getUser();
        Submission submission = submissionService.getFullSubmission(submissionId);
        if (!Role.ADMIN.equals(user.getRole()) && !user.getId().equals(submission.getUserId())) {
            throw new RuntimeException("Access denied");
        }
        return submission;
    }

    @GetMapping("contest/{contestId}/submitParameters")
    @AccessLevel(role = Role.USER, contestStatus = ContestStatus.STARTED)
    public SubmitParametersResponse getSubmitParameters(@PathVariable Long contestId) {
        SubmitParametersResponse result = new SubmitParametersResponse();
        Role role = authenticationHolder.getUser().getRole();
        boolean withHidden = role == Role.ADMIN;
        result.setProblems(problemService.getContestProblems(contestId, withHidden));
        result.setLanguages(programmingLanguageService.getAllProgrammingLanguages());
        return result;
    }

    @GetMapping("contest/{id}/all-submissions")
    @AccessLevel(role = Role.ADMIN)
    public List<SubmissionResponse> getContestSubmissions(@PathVariable("id") Long contestId) {
        return submissionService.getContestSubmissions(contestId);
    }

    @GetMapping("all-submissions")
    @AccessLevel(role = Role.ADMIN)
    public List<SubmissionResponse> getAllSubmissions() {
        return submissionService.getAllSubmissions();
    }
}
