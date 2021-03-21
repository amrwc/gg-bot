package dev.amrw.bin;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(
        name = "run",
        description = "Build and run GG Bot Docker container."
)
public class Run implements Callable<Integer> {

    @Parameters(index = "0")
    private String scriptName;
    @Parameters(index = "1")
    private String foo;
    @Parameters(index = "2")
    private String bar;

    public static void main(final String[] argv) {
        System.out.println("I think it worked: " + Arrays.toString(argv));
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("I think it worked: " + foo + " " + bar);
        return 0;
    }
}
