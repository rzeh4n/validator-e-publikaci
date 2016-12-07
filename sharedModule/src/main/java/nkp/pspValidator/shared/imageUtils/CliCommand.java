package nkp.pspValidator.shared.imageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Martin Řehánek on 29.9.16.
 */
public class CliCommand {

    private final String command;
    private final boolean prettyPrint;

    public CliCommand(String command) {
        this(command, false);
    }

    public CliCommand(String command, boolean prettyPrint) {
        this.command = command;
        this.prettyPrint = prettyPrint;
    }


    public Result execute() throws IOException, InterruptedException {
        try {
            //System.out.println("command: " + this);
            Process process = Runtime.getRuntime().exec(command);
            //TODO: perhaps thread pooling here

            //read standard error stream in separate thread
            StringBuilder stderrBuilder = new StringBuilder();
            Thread errThread = new Thread(() -> {
                try {
                    BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = stderrReader.readLine()) != null) {
                        stderrBuilder.append(line);
                        if (prettyPrint) {
                            stderrBuilder.append('\n');
                        }
                    }
                    stderrReader.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    //nothing, only main thread running program itself shoud propagate exception
                }
            });

            //read standard output stream in separate thread
            StringBuilder stdoutBuilder = new StringBuilder();
            Thread outThread = new Thread(() -> {
                try {
                    BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        stdoutBuilder.append(line);
                        if (prettyPrint) {
                            stdoutBuilder.append('\n');
                        }
                        //FIXME: on macOS the whole output is single line
                        //System.err.println(line);
                    }
                    stdoutReader.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    //nothing, only main thread running program itself shoud propagate exception
                }
            });
            //start threads to read output streams
            outThread.start();
            errThread.start();

            //wait for all 3 threads to finish
            int exitValue = process.waitFor();
            outThread.join();
            errThread.join();

            String stdOut = stdoutBuilder.toString();
            String stdErr = stderrBuilder.toString();
            /*System.err.println("SOUT: " + stdOut);
            System.err.println("SERR: " + stdErr);*/
            Result result = new Result(exitValue, stdOut, stdErr);
            //result.print();
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public String toString() {
        return "CliCommand{" +
                "command='" + command + '\'' +
                ", prettyPrint=" + prettyPrint +
                '}';
    }

    public static class Result {
        private final int exitValue;
        private final String stdout;
        private final String stderr;

        public Result(int exitValue, String stdout, String stderr) {
            this.exitValue = exitValue;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public int getExitValue() {
            return exitValue;
        }

        public void print() {
            System.err.println("EXIT VALUE: " + exitValue + (exitValue == 0 ? " (ok)" : " (error)"));
            System.err.println("SERR: " + stderr);
            System.err.println("SOUT: " + stdout);
        }
    }


}
