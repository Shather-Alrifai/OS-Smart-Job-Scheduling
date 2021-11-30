/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author best2
 */
public class Job {
    int JobArrvTime ;//arrivalTime
    int JobID; //job ID
    int JobMemS; //memory space required for each job 
    int JobDevice; //number of Devices required for each job
    int JobBT; //job burst time 
    int JobPriority; //job priority
    int JobST;//job start time 
    int JobFT; // job finish time
    int JobTAT; // job Turn around time
    int JobAccuredT;// job accumulated time 
    int RemBT;//remaining BT 
    int WaitT; //waiting time
    int EnterQ2time; // when it entered Q2 time. 
     private int JobWeight;// job weight depends on its priority
     private boolean isTermenated= false;
     private boolean isNew= true;
     
    public Job (){}
    
    public Job(int JobArrvTime, int JobID, int JobMemS, int JobDevice, int JobBT, int JobPriority) {
        this.JobArrvTime = JobArrvTime;
        this.JobID = JobID;
        this.JobMemS = JobMemS;
        this.JobDevice = JobDevice;
        this.JobBT = JobBT;
        this.JobPriority = JobPriority;
        this.JobFT = 0;
        this.JobST=0;
        this.JobTAT = 0;
        this.JobAccuredT = 0;
        // set process weight according to its priority
        this.JobWeight = this.JobPriority == 1 ? 2 : 1;
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
        return this.JobTAT=this.JobFT -this.JobArrvTime;
        
    }
 
    public void setJobTAT(int JobTAT) {
        this.JobTAT = JobTAT;
    }
    
    //-------------------------------------------------
    public int getJobAccuredT() {
        return this.JobAccuredT;
    }

    public void setJobAccuredT(int JobAccuredT) {
        this.JobAccuredT+= JobAccuredT;
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

    public boolean isIsTermenated() {
        return isTermenated;
    }

    public void setIsTermenated(boolean isTermenated) {
        this.isTermenated = isTermenated;
    }
    
    
    
    //-------------------------------------------------

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    
    //----------------------printing the Job info----------------------------------

    
    @Override
    public String toString() {
        //Job ID   Arrival Time    Finish Time  Turnaround Time 
        return "\t"+this.JobID+"\t"+this.JobArrvTime+"\t"+this.JobFT+"\t"+this.JobTAT;
    }
   
}
