import java.lang.instrument.Instrumentation;

public class LoggerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("In LoggerAgent.");
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
    }
}