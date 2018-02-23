package eskimo.backend.containers;

import eskimo.backend.domain.Problem;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class ProblemContainer {

    private Problem problem;
    private File checker;
    private File validator;
    private List<SolutionContainer> solutions;
    private List<TestContainer> tests;
    private List<StatementContainer> statements;

}