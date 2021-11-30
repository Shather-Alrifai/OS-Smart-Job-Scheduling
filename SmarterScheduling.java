/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author best2
 */
import java.io.*;
import java.util.*;

public class SmarterScheduling {

    //all job Q
    public static Queue<Job> AllJobs = new LinkedList<Job>();
    //Q1 ,shortest priorityQ
    public static PriorityQueue<Job> HoldQ1 = new PriorityQueue<Job>(new Comparator<Job>() {
        @Override
        public int compare(Job Pw1, Job Pw2) {
            if (Pw1.getJobBT() == Pw2.getJobBT()) {
                if (Pw1.getJobArrvTime() < Pw2.getJobArrvTime()) {
                    return -1;
                } else if (Pw1.getJobArrvTime() > Pw2.getJobArrvTime()) {
                    return 1;
                }
                return 0;

            } else if (Pw1.getJobBT() < Pw2.getJobBT()) {
                return -1;
            } else {
                return 1;
            }
        }
    });

    //Q2 , longest Linkedlist
    public static Queue<Job> HoldQ2 = new LinkedList<Job>();

    //Q3 ,waiting linked list
    public static Queue<Job> HoldQ3 = new LinkedList<Job>();

    //completed job Q linkedlist
    public static Queue<Job> CompletedQ = new LinkedList<Job>();

    //Main memory of the system
    static int TotalMemo = 0;
    //remaining Available memory
    static int AvailMemo = 0;

    //System start time of the system
    static int StartTime = 0;

    //System current time
    static int CurrentTime = 0;

    //All devices in system
    static int TotalDevs = 0;

    //Available Devices in sysytem
    static int AvailDevs = 0;

    //number of jobs read from input file
    static int TotalJobs;

    //Time Quantum of Rounds robin,must be calculated
    static int TQuantum = 0;

    //i
    static int i;
    //e
    static int e;

    //SR weighted sum of the remaining burst times in the ready queue
    static int SR;
    //AR weighted average of the burst times
    static int AR;
    //job executing in CPU
    static Job ExcJob = null;

//Main method 
    public static void main(String[] args) throws FileNotFoundException {
        //create file object
        File inFile = new File("input3.txt");
        if (!inFile.exists()) {
            System.out.println("not exists");
            System.exit(0);
        }// check if file exists or not .
        // read from input file , write in outputFile .  
        Scanner input = new Scanner(inFile);
        File outFile = new File("outputFile.txt");
        PrintWriter output = new PrintWriter(outFile);
        //------------------------------------------------------------

        // here we have 2 nested loops ..outer loop that invokes all methods, inner loop that read and creates jobs 
        while (input.hasNext()) {
            //C 0 M=100 L=10 S=10 Q=2
            String command = input.next();

            StartTime = input.nextInt();
            TotalMemo = Integer.parseInt(input.next().substring(2));
            AvailMemo = TotalMemo;

            TotalDevs = Integer.parseInt(input.next().substring(2));
            AvailDevs = TotalDevs;

            //------------------------------------------------------------------
            Job job;
            TotalJobs = 0;

            int time = 0;

            while (input.hasNext()) {
                String next = input.next();
                //  A 1 J=1 M=30 S=0 R=10 P=1 

                if (next.equals("A")) {//if A
                    int ArrvTime = Integer.parseInt(input.next());
                    int JobID = Integer.parseInt(input.next().substring(2));
                    int JobMemS = Integer.parseInt(input.next().substring(2));
                    int JobDevice = Integer.parseInt(input.next().substring(2));
                    int JobBT = Integer.parseInt(input.next().substring(2));
                    int JobPriority = Integer.parseInt(input.next().substring(2));
                    // If there is not enough total main memory or total number of 
                    //devices in the system for the job, the job is rejected never gets to one of the Hold Queues
                    if (JobMemS <= TotalMemo && JobDevice <= TotalDevs) {
                        // create new job object
                        job = new Job(ArrvTime, JobID, JobMemS, JobDevice, JobBT, JobPriority);
                        AllJobs.add(job);
                        TotalJobs++;

                    }//second if

                } //------------------------------------------------------------------------------------------------------
                else if (next.equals("D")) {
                    int Dtime = input.nextInt();
                    if (Dtime < 999999) {
                        job = new Job(Dtime, 0, 999999, 999999, 999999, 999999);
                        AllJobs.add(job);
                    } else {
                        time = Dtime;
                        break;

                    }

                }
            }//end of inner loop 

            //loop to raed the jobs from all jobQ
            for (Job J : AllJobs) {
                if (J.JobMemS <= AvailMemo && J.JobDevice <= AvailDevs) {
                    //invoke task0(Job J)

                } else//put in Q3 J.JobMemS >AvailMemo || J.JobDevice>AvailDevs
                {
                    HoldQ3.add(J);
                }
            }

            //ENTER FIRST JOB DIRECT TO THE CPU
        }//end of outer loop

        input.close();
        output.close();
    }//end main

    public static void externalEvent() {
        
        
            // work on all_jobs queue
        if (!AllJobs.isEmpty()) {
           Job job = AllJobs.poll();
            // in case of "D" job
            if (job.getJobID() == 0) {
                //invoke displayEvent
                
            } else {  // in case of "A" job
                // if there were available main memory and devices 
                // the job is sent to hold queue 1 (ready queue)
                if (job.getJobMemS() <= AvailMemo
                        && job.getJobDevice() <= AvailDevs) {
                    AvailMemo -= job.getJobMemS();
                    AvailDevs -= job.getJobDevice();
                    
                   //add to Q1  Hold1_DRR(job);
                   
                    // update SR& AR
                    SR_AR_update();
                } else {
                    // if there were not available main memory and devices
                    // the job is sent to hold queue 2 (waiting queue)
                    //invoke add to Hold Q2
                    // save the entred time
                    job.setEnterQ2time(CurrentTime);
                }
            }
        }
         
        
        
        
    }

    public static void internalEvent() {
        Jobterminate();
        if (!HoldQ1.isEmpty()) {
            Job job = HoldQ1.poll();
             SR_ARupdate(ExcJob);
             putInCPU(job);
        }
        
        
    }

    public static void Jobterminate() {
        
             if (ExcJob != null) {
            if (ExcJob.getRemBT() == 0) {
                //the job releases any main memory
                CompletedQ.add(ExcJob);
                AvailDevs+= ExcJob.getJobDevice();
                AvailMemo+= ExcJob.getJobMemS();
                 ExcJob = null;
                
                //invoke task1 task2
               
            } else {
                HoldQ1.add(ExcJob);
                SR_ARupdate(ExcJob);
                if (HoldQ1.size() == 1) {
                    TQuantum = HoldQ1.peek().getRemBT();
                    putInCPU(HoldQ1.poll());
                    SR_ARupdate(ExcJob);
                }
            }
        }
        
         
    }

    public static void putInCPU(Job CPUjob) {
        ExcJob= CPUjob;
       ExcJob.setJobST(CurrentTime);
         DynamicTQuantum();
         
        if (ExcJob.getRemBT() > TQuantum) {
            ExcJob.setJobFT(CurrentTime + TQuantum);
            ExcJob.setRemBT(ExcJob.getRemBT() - TQuantum);
        } else {
           // int finish = Math.min(exe_job.getNeedTime(), quantum);
             
            ExcJob.setJobFT(CurrentTime + ExcJob.getRemBT());
            ExcJob.setRemBT(0);
            ExcJob.setJobTAT(ExcJob.getJobFT() - ExcJob.getJobArrvTime());
        }
        
    }

    public static void SR_AR_update() {
        if (HoldQ1.isEmpty()) {
            AR = 0;
        }
        SR = 0;
        for (Job j : HoldQ1) {
            SR += (j.getJobWeight() * j.getRemBT());
        }

        AR = (SR / HoldQ1.size());
        //   TQuantum = AR;
       // DynamicTQuantum();

    }
    
    
public static void DynamicTQuantum(){
TQuantum=Math.min(AR,ExcJob.getRemBT());
}

    ////////////////////// Implementation of the Hold Queue 1 based on Dynamic Round Robin //////////////////////
    public static void inHoldQ1_DRR(Job job) {
    }

    public static void HoldQ2_DP() {//Task2

    }

    public static void SortHoldQ2() {
    }

    public static void Task0(Job J) {
//BTi is the process burst time
//AvgBT is the average burst time of all processes in Hold Queue 1
//X and Y represent available Memory and Devices, respectively.
        int BTi = J.JobBT;
        int AvgBT = 0;
        int sumBT = 0;
        if (HoldQ1.isEmpty()) {
            AvgBT = BTi;
            HoldQ1.add(J);
        } else {
            // we can make it a method
            //Compute AvgBT in Q1
            for (Job job : HoldQ1) {
                sumBT += job.JobBT;
            }
            AvgBT = sumBT / HoldQ1.size();
        }

        if (BTi > AvgBT) {//put the process in Hold Queue 2
            HoldQ2.add(J);
//X=X- Processi. RequestedMemory

            AvailMemo -= J.JobMemS;
//Y=Y- Processi. RequestedDevices
            AvailDevs -= J.JobDevice;

        } else { //put the process in Hold Queue 1

            HoldQ1.add(J);

//X=X- Processi. RequestedMemory
            AvailMemo -= J.JobMemS;
//Y=Y- Processi. RequestedDevices
            AvailDevs -= J.JobDevice;
        }

    }

    public static void Task1() {
    }

    public static void finalDisplay(Queue<Job> allJobs) {
    }

    public static void displayEvent(Job job) {
    }

}
