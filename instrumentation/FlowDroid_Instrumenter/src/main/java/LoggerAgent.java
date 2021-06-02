import java.lang.instrument.Instrumentation;

public class LoggerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
    }
}