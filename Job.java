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
    
    
    //----------------------printing the Job info----------------------------------

    
    @Override
    public String toString() {
        //Job ID   Arrival Time    Finish Time  Turnaround Time 
        return "\t"+this.JobID+"\t"+this.JobArrvTime+"\t"+this.JobFT+"\t"+this.JobTAT;
    }
   
}
