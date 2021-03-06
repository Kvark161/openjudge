package eskimo.invoker.services;

import eskimo.invoker.config.InvokerSettingsProvider;
import eskimo.invoker.entity.*;
import eskimo.invoker.enums.CompilationVerdict;
import eskimo.invoker.executers.TesterWindows;
import eskimo.invoker.utils.InvokerUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class ExecuteServiceWindows implements ExecuteService {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteServiceWindows.class);

    private final InvokerUtils invokerUtils;
    private final InvokerSettingsProvider invokerSettings;

    public ExecuteServiceWindows(InvokerUtils invokerUtils, InvokerSettingsProvider invokerSettings) {
        this.invokerUtils = invokerUtils;
        this.invokerSettings = invokerSettings;
    }

    @Override
    public CompilationResult compile(CompilationParams compilationParams) {
        logger.info("begin compilation");
        File folder = null;
        try {
            folder = invokerUtils.createRunnerTempFolder("compile-");
            logger.info("prepare folder for compilation folder: \"" + folder.getAbsolutePath() + "\"");
            File sourceFile = new File(folder.getAbsolutePath() + File.separator + compilationParams.getSourceFileName());
            File executableFile = new File(folder.getAbsolutePath() + File.separator + compilationParams.getExecutableFileName());
            File stdout = new File(folder.getAbsolutePath() + File.separator + "stdout.txt");
            File stderr = new File(folder.getAbsolutePath() + File.separator + "stderr.txt");
            File stat = new File(folder.getAbsolutePath() + File.separator + "stat.txt");
            String testLibPath = "";
            if (compilationParams.getTestLib() != null) {
                File testLib = new File(folder.getAbsolutePath() + File.separator + compilationParams.getTestLibName());
                FileUtils.writeStringToFile(testLib, compilationParams.getTestLib());
                testLibPath = testLib.getAbsolutePath();
            }
            FileUtils.writeStringToFile(sourceFile, compilationParams.getSourceCode());
            List<String> commands = compilationParams.prepareCompilationCommand(sourceFile.getAbsolutePath(), executableFile.getAbsolutePath(), testLibPath);
            logger.info("compile submission");
            ExecutionResult executionResult = invokerUtils.executeRunner(commands, null, stdout, stderr, stat, compilationParams.getTimeLimit(), compilationParams.getMemoryLimit(), folder, true);
            logger.info("prepare compilation result");
            CompilationResult compilationResult = new CompilationResult();
            if (stdout.exists()) {
                compilationResult.setCompilerStdout(FileUtils.readFileToString(stdout));
            }
            if (stderr.exists()) {
                compilationResult.setCompilerStderr(FileUtils.readFileToString(stderr));
            }
            if (executionResult.getExitCode() == 0) {
                compilationResult.setVerdict(CompilationVerdict.SUCCESS);
                compilationResult.setExecutable(FileUtils.readFileToByteArray(executableFile));
            } else {
                compilationResult.setVerdict(CompilationVerdict.COMPILATION_ERROR);
            }
            return compilationResult;
        } catch (Throwable e) {
            logger.error("Error while compiling", e);
            CompilationResult compilationResult = new CompilationResult();
            compilationResult.setVerdict(CompilationVerdict.INTERNAL_INVOKER_ERROR);
            return compilationResult;
        } finally {
            logger.info("finish compilation");
            if (folder != null && invokerSettings.deleteTempFiles()) {
                try {
                    FileUtils.deleteDirectory(folder);
                } catch (IOException e) {
                    logger.warn("Can't delete directory after compilation: " + folder.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public TestResult[] test(AbstractTestParams testParams) {
        return new TesterWindows(invokerUtils, invokerSettings, testParams).test();
    }

}
