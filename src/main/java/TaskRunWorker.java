public class TaskRunWorker implements Runnable {


    int workerCount;

    public TaskRunWorker(int workerCount) {
        this.workerCount = workerCount;
    }

    @Override
    public void run() {
        EC2.runMachines("worker", Integer.toString(workerCount));
    }
}
