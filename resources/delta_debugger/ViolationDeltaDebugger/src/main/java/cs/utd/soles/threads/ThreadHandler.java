package cs.utd.soles.threads;

//TODO:: if we have a new modification to make, is this easy to implement?
public interface ThreadHandler {


    enum ProcessType{
        CREATE_APK_PROCESS,
        AQL_PROCESS1,
        AQL_PROCESS2,
        AQL_RUN,
        CALLGRAPH
    }
    void handleThread(Thread thread, ProcessType type, String finalString, String finalString2);
}
