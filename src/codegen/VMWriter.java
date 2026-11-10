package codegen;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VMWriter {

    public enum Segment {
        CONST("constant"),
        ARG("argument"),
        LOCAL("local"),
        STATIC("static"),
        THIS("this"),
        THAT("that"),
        POINTER("pointer"),
        TEMP("temp");

        private final String vmName;

        Segment(String vmName) {
            this.vmName = vmName;
        }

        public String getVmName() {
            return vmName;
        }
    }

    public enum Command {
        ADD("add"),
        SUB("sub"),
        NEG("neg"),
        EQ("eq"),
        GT("gt"),
        LT("lt"),
        AND("and"),
        OR("or"),
        NOT("not");

        private final String vmName;

        Command(String vmName) {
            this.vmName = vmName;
        }

        public String getVmName() {
            return vmName;
        }
    }

    private final PrintWriter writer;

    public VMWriter(File outputFile) throws FileNotFoundException {
        this.writer = new PrintWriter(outputFile);
    }

    public void writePush(Segment segment, int index) {
        writer.println("push " + segment.getVmName() + " " + index);
    }

    public void writePop(Segment segment, int index) {
        writer.println("pop " + segment.getVmName() + " " + index);
    }

    public void writeArithmetic(Command command) {
        writer.println(command.getVmName());
    }

    public void writeLabel(String label) {
        writer.println("label " + label);
    }

    public void writeGoto(String label) {
        writer.println("goto " + label);
    }

    public void writeIf(String label) {
        writer.println("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        writer.println("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nVars) {
        writer.println("function " + name + " " + nVars);
    }

    public void writeReturn() {
        writer.println("return");
    }

    public void close() {
        writer.close();
    }
}