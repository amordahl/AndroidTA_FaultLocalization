package cs.utd.soles;

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
