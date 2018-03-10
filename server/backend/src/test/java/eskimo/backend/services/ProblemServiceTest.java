package eskimo.backend.services;

import eskimo.backend.domain.Contest;
import eskimo.backend.domain.Problem;
import eskimo.backend.parsers.ProblemParserPolygonZip;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class ProblemServiceTest {
    @Autowired
    private ContestService contestService;
    @Autowired
    private ProblemService problemService;

    @Test
    public void addProblemFromZip() {
        URL testZip = getClass().getClassLoader().getResource("problems/a-plus-b/standard.zip");
        if (testZip == null) {
            throw new RuntimeException("There is no zip for test");
        }
        String zipPath = testZip.getFile();

        Contest contest = createTestContest();
        Long problemId = problemService.addProblemFromZip(contest.getId(), new File(zipPath));
        Problem expected = new Problem();
        expected.setId(problemId);
        expected.setIndex(1L);
        expected.setContestId(contest.getId());
        expected.setTimeLimit(ProblemParserPolygonZip.DEFAULT_TIME_LIMIT);
        expected.setMemoryLimit(ProblemParserPolygonZip.DEFAULT_MEMORY_LIMIT);
        expected.setTestsCount(0);

        Problem actual = problemService.getProblemById(problemId);
        assertThat("Problem should be added correctly", actual, is(expected));
    }

    private Contest createTestContest() {
        Contest contest = new Contest();
        contest.setName("test-contest");
        return contestService.createContest(contest);
    }
}