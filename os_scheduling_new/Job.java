/*
CPCS361 ,section:IAR
project: Smart process scheduling 
Group 3
members:
Shater Mohammed Al-shubbak, Reem Abdulrahman Alghamdi, Raneem Hameed Alguraygri, Noha Abbas sukkar
Date: 12/12/2021

compiler:
 JDK 8.1, internal API of javac

hardware configuration:
 processor: Intel(R) Core(TM) i7-10510U CPU @ 1.80GHz   2.30 GHz
 RAM: 16.0 GB 
 system type: 64-bit operating system, x64-based processor

operating system:
 Windows 10
*/
package os_scheduling_new;
public class Job {

    private int JobArrvTime;//arrivalTime
    private int JobID; //job ID
    private int JobMemS; //memory space required for each job 
    private int JobDevice; //number of Devices required for each job
    private int JobBT; //job burst time 
    private int JobPriority; //job priority
    private int JobST;//job start time 
    private int JobFT; // job finish time
    private int JobTAT; // job Turn around time

    private int RemBT;//remaining BT 
    private int WaitT; //waiting time
    private int EnterQ2time; // when it entered Q2 time. 
    private int JobWeight;// job weight depends on its priority
    private Double DynamicPriority;
    private int execution_time;
    
//-----------Job Constructor--------------------------------------
    public Job(int JobArrvTime, int JobID, int JobMemS, int JobDevice, int JobBT, int JobPriority) {
        this.JobArrvTime = JobArrvTime;
        this.JobID = JobID;
        this.JobMemS = JobMemS;
        this.JobDevice = JobDevice;
        this.JobBT = JobBT;
        this.JobPriority = JobPriority;
        this.JobFT = 0;
        this.JobST = 0;
        this.JobTAT = 0;
        this.RemBT = JobBT;
        this.DynamicPriority = -1.0;

        // set process weight according to its priority
        this.JobWeight = this.JobPriority == 1 ? 2 : 1;
    }
//--------------D job constructor--------------------------------
    //
    public Job(int Dtime) {
        this.JobID = 0;
        this.JobArrvTime = Dtime;
    }

   

    //-------------------------------------------------
    public int getJobArrvTime() {
        return JobArrvTime;
    }

    public void setJobArrvTime(int JobArrvTime) {
        this.JobArrvTime = JobArrvTime;
    }

    //-------------------------------------------------
    public int getJobID() {
        return JobID;
    }

    public void setJobID(int JobID) {
        this.JobID = JobID;
    }

    //-------------------------------------------------
    public int getJobMemS() {
        return JobMemS;
    }

    public void setJobMemS(int JobMemS) {
        this.JobMemS = JobMemS;
    }

    //-------------------------------------------------
    public int getJobDevice() {
        return JobDevice;
    }

    public void setJobDevice(int JobDevice) {
        this.JobDevice = JobDevice;
    }

    //-------------------------------------------------
    public int getJobBT() {
        return JobBT;
    }

    public void setJobBT(int JobBT) {
        this.JobBT = JobBT;
    }

    //-------------------------------------------------
    public int getJobPriority() {
        return JobPriority;
    }

    public void setJobPriority(int JobPriority) {
        this.JobPriority = JobPriority;
    }

    //-------------------------------------------------
    public int getJobFT() {
        return this.JobFT;
    }

    public void setJobFT(int JobFT) {
        this.JobFT = JobFT;
    }

    //-------------------------------------------------
    public int getJobST() {
        return this.JobST;
    }

    public void setJobST(int JobST) {
        this.JobST = JobST;
    }

    //-------------------------------------------------
    public int getJobTAT() {
        //Turn around time= finish time- arrival time
        return this.JobTAT = this.JobFT - this.JobArrvTime;

    }

    public void setJobTAT(int JobTAT) {
        this.JobTAT = JobTAT;
    }

 
    //-------------------------------------------------
    public int getEnterQ2time() {
        return EnterQ2time;
    }

    public void setEnterQ2time(int EnterQ2time) {
        this.EnterQ2time = EnterQ2time;
    }
    //-------------------------------------------------

    public int getRemBT() {
        return RemBT;
    }

    public void setRemBT(int RemBT) {
        this.RemBT = RemBT;
    }

    //-------------------------------------------------
    public int getWaitT() {
        return WaitT;
    }

    public void setWaitT(int WaitT) {
        this.WaitT = WaitT;
    }
    //-------------------------------------------------

    public int getJobWeight() {
        return JobWeight;
    }

    public void setJobWeight(int JobWeight) {
        this.JobWeight = JobWeight;
    }
    //-------------------------------------------------

    public double getDynamicPriority() {
        return DynamicPriority;
    }

    public void setDynamicPriority(double DynamicPriority) {
        this.DynamicPriority = DynamicPriority;
    }
//-------------------------------------------------

    public int getExecution_time() {
        return execution_time;
    }

    public void setExecution_time(int execution_time) {
        this.execution_time = execution_time;
    }

    


}
