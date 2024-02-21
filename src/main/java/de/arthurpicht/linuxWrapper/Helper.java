package de.arthurpicht.linuxWrapper;

import de.arthurpicht.processExecutor.*;
import de.arthurpicht.utils.core.collection.Lists;
import de.arthurpicht.utils.core.strings.Strings;

import java.util.Arrays;
import java.util.List;

public class Helper {

    public static void commandLogging(String[] command, LoggingConfig loggingConfig) {
        List<String> commandList = Arrays.asList(command);
        String commandString = "> " + Strings.listing(commandList, " ");
        if (loggingConfig.hasLogger()) {
            loggingConfig.logger.atLevel(loggingConfig.getLogLevelStd()).log(commandString);
        }
        if (loggingConfig.outputToConsole) {
            System.out.println(commandString);
        }
    }

    public static ProcessResultCollection execute(String[] command, LoggingConfig loggingConfig, boolean assertSuccess) {
        StandardOutHandler standardOutHandler = loggingConfig.getGeneralStandardOutHandler();
        StandardErrorHandler standardErrorHandler = loggingConfig.getGeneralStandardErrorHandler();
        try {
            ProcessResultCollection result = ProcessExecution.execute(standardOutHandler, standardErrorHandler, command);
            if (result.isSuccess()) {
                return result;
            } else {
                if (assertSuccess) {
                    throw Helper.createException(command, result);
                } else {
                    return result;
                }
            }
        } catch (ProcessExecutionException e) {
            String commandString = Strings.listing(Lists.newArrayList(command), " ");
            throw new LinuxWrapperCoreRuntimeException("Process execution of '" + commandString + "' failed: " + e.getMessage(), e);
        }
    }

    public static LinuxWrapperCoreRuntimeException createException(String[] command, ProcessResultCollection result) {
        if (result.isSuccess()) throw new IllegalStateException("Process execution not failed.");
        String commandString = Strings.listing(Lists.newArrayList(command), " ");
        if (!result.getErrorOut().isEmpty()) {
            return new LinuxWrapperCoreRuntimeException(
                    "Command failed with exit code " + result.getExitCode() + ": [" + commandString + "]. "
                    + result.getErrorOut().get(0));
        } else {
            return new LinuxWrapperCoreRuntimeException(
                    "Command failed with exit code " + result.getExitCode() + ": [" + commandString + "].");
        }
    }

}