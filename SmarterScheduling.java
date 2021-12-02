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

    static int TotalMemo = 0;//Main memory of the system
    static int AvailMemo = 0;//remaining Available memory
    static int StartTime = 0;//System start time of the system
    static int CurrentTime = 0;//System current time
    static int TotalDevs = 0;//All devices in system
    static int AvailDevs = 0;//Available Devices in sysytem
    static int TotalJobs = 0;//number of jobs read from input file
    static int TQuantum = 0;//Time Quantum of Rounds robin,must be calculated
    //i
    static int i = 0;
    //e
    static int e = 0;
    static int SR = 0;   //SR weighted sum of the remaining burst times in the ready queue
    static int AR = 0;//AR weighted average of the burst times
    static Job ExcJob = null; //job executing in CPU
    static int Dtime = 0;
    static int DisplayTime = 0;
    static final int Infinity = 999999;
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

        String inputLn;
        String[] command;
        Job job;

        // here we have 2 nested loops ..outer loop that invokes all methods, inner loop that read and creates jobs 
        while (input.hasNextLine()) {
            // read line by line from the input file
            inputLn = input.nextLine().replaceAll("[a-zA-Z]=", "");
            // separate the info in an array
            command = inputLn.split(" ");
            //------------------------------------------------------------------
            // read system configuration   C 0 M=100 L=10 S=10 Q=2

            if (command[0].equals("C")) {
                StartTime = Integer.parseInt(command[1]);
                //  System.out.println(command[1]);
                CurrentTime = StartTime;
                TotalMemo = Integer.parseInt(command[2]);
                //   System.out.println(command[2]);
                TotalDevs = Integer.parseInt(command[3]);
                //  System.out.println(command[3]);
                AvailMemo = TotalMemo;
                AvailDevs = TotalDevs;
//--------------------------------------------------------------
                // read "A" jobs
            } else if (command[0].equals("A")) {
                int ArrvTime = Integer.parseInt(command[1]);
                int JobID = Integer.parseInt(command[2]);
                int JobMemS = Integer.parseInt(command[3]);
                int JobDevice = Integer.parseInt(command[4]);
                int JobBT = Integer.parseInt(command[5]);
                int JobPriority = Integer.parseInt(command[6]);
                // create process for all valid jobs then add them to all_jobs queue
                if (JobMemS <= TotalMemo && JobDevice <= TotalDevs) {
                    // create new job object
                    job = new Job(ArrvTime, JobID, JobMemS, JobDevice, JobBT, JobPriority);
                    AllJobs.add(job);
                    TotalJobs++;//to count the number of job entered to the queue
                }

            } // read "D" job
            else if (command[0].equals("D")) {
                int Dtime = Integer.parseInt(command[1]);
                DisplayTime = Dtime;

                if (Dtime != Infinity) {
                    // add job D to valid jobs Q
                    job = new Job(Dtime, 0, Infinity, Infinity, Infinity, Infinity);
                    AllJobs.add(job);

                    // invoke dispaly system state
                } else {
                    //Dtime ==999999 Infinity
                    // invoke start method.....
                    StartSystem();

                    //loop to read the jobs from all jobQ
                    for (Job J : AllJobs) {
                        //invoke task0(Job J)
                        Task0(J);
                    }

                    //----------------------------------------------------------
                    // print system final state& reset variables
                    if (DisplayTime == Infinity && CompletedQ.size() == TotalJobs) {
                        finalDisplay(output);
                        CompletedQ.clear();
                        HoldQ1.clear();
                        HoldQ2.clear();
                        AllJobs.clear();
                        ExcJob = null;
                        StartTime = 0;
                        CurrentTime = 0;
                        TQuantum = 0;
                        TotalJobs = 0;
                        DisplayTime = 0;
                        SR = 0;
                        AR = 0;
                    }

                }

            }//end of outer loop

            input.close();
            output.close();
        }
    }//end main

    public static void StartSystem() {
        //send the 1st job to cpu
        Job firstJob = AllJobs.poll();
        CurrentTime = firstJob.getJobArrvTime();
        // allocate memory& devices to the job
        // available memory = system main memory - first job requested memory
        AvailMemo = TotalMemo - firstJob.getJobMemS();
        // available devices = system serial devices - first job requested devices
        AvailDevs = TotalMemo - firstJob.getJobDevice();
        //ENTER FIRST JOB DIRECT TO THE CPU
        putInCPU(firstJob);

        //clc I,E and compare
        while (CompletedQ.size() != TotalJobs) {
            if (AllJobs.isEmpty()) {
                i = Infinity;
            } else {
                i = AllJobs.peek().getJobArrvTime();
            }
            if (ExcJob == null) {
                e = Infinity;
            } else {
                e = ExcJob.getJobFT();
            }
            // update system time each iteration
            CurrentTime = Math.min(i, e);
            // the system works acccording to i and e values
            if (i < e) {
                externalEvent();
            } else if (i > e) {
                internalEvent();
            } else {
                // if i == e
                // perform internal events before external events
                internalEvent();
                externalEvent();
            }
        }

    }

    public static void externalEvent() {//90%done

        // work on all_jobs queue
        if (!AllJobs.isEmpty()) {
            Job job = AllJobs.poll();
            // in case of "D" job
            if (job.getJobID() == 0) {
                //invoke displayEvent

            } else if (job.getJobMemS() <= AvailMemo
                    && job.getJobDevice() <= AvailDevs) {
                //invoke task0 
                Task0(job);
            } else {//  else if (J.JobMemS > AvailMemo || J.JobDevice > AvailDevs) {

                  HoldQ3.add(job);
            }
        }
    }

    public static void internalEvent() {
        Jobterminate();
        if (!HoldQ1.isEmpty()) {
            Job job = HoldQ1.poll();
            SR_AR_update();
            putInCPU(job);
        }

    }

    public static void Jobterminate() {//95% done

        if (ExcJob != null) {
            if (ExcJob.getRemBT() == 0) {
                //the job is released from cpu , returns the resources 
                CompletedQ.add(ExcJob);
                AvailDevs += ExcJob.getJobDevice();
                AvailMemo += ExcJob.getJobMemS();
                // no jobs in CPU
                ExcJob = null;

                //invoke task1 task2
                Task1();
                HoldQ2_DP(); //Task2
            } else {

                inHoldQ1_DRR(ExcJob);//DRR 
                SR_AR_update();
                if (HoldQ1.size() == 1) {
                    TQuantum = HoldQ1.peek().getRemBT();
                    putInCPU(HoldQ1.poll());
                    SR_AR_update();
                }
            }
        }

    }

    public static void putInCPU(Job CPUjob) {
        ExcJob = CPUjob;
        // set quantum time
        DynamicTQuantum();
        // set the job start time of execution
        ExcJob.setJobST(CurrentTime);

        if (ExcJob.getRemBT() > TQuantum) {
            ExcJob.setJobFT(CurrentTime + TQuantum);
            ExcJob.setRemBT(ExcJob.getRemBT() - TQuantum);

        } else {//ExcJob.getRemBT() < = TQuantum

            // int finish = Math.min(exe_job.getNeedTime(), quantum);
            ExcJob.setJobFT(CurrentTime + ExcJob.getRemBT());
            ExcJob.setRemBT(0);
            ExcJob.setJobTAT(ExcJob.getJobFT() - ExcJob.getJobArrvTime());
        }

    }

    public static void SR_AR_update() {//done
        if (HoldQ1.isEmpty()) {
            AR = 0;
        } else {
            SR = 0;
            for (Job j : HoldQ1) {
                SR += (j.getJobWeight() * j.getRemBT());
            }

            AR = (SR / HoldQ1.size());

        }
    }

    public static void DynamicTQuantum() {
        TQuantum = Math.min(AR, ExcJob.getRemBT());
    }

    ////////////////////// Implementation of the Hold Queue 1 based on Dynamic Round Robin //////////////////////
    public static void inHoldQ1_DRR(Job job) {//80% done
        if (HoldQ1.isEmpty()) {
            // if there is no jobs in hold queue 1
            // the current executing job takes its time to finish
            TQuantum = ExcJob.getRemBT();
            // set the job start time of execution
            ExcJob.setJobST(CurrentTime);
            // set the executing job finish time

            ExcJob.setJobFT(CurrentTime + TQuantum);
            //add the job to Q1
            HoldQ1.add(job);
            // update SR& AR
            SR_AR_update();
        } else {
            HoldQ1.add(job);
            // update SR& AR
            SR_AR_update();

            // executing job is sent to hold queue 1 (ready queue)
            HoldQ1.add(ExcJob);
            // update SR& AR
            SR_AR_update();
            // start executing the next job
            Job j = HoldQ1.poll();

            // send next job to CPU
            putInCPU(j);
            // set quantum time
            DynamicTQuantum();
            // set the job start time of execution
            ExcJob.setJobST(CurrentTime);
            // set the executing job finish time
            int finish = Math.min(ExcJob.getRemBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + finish);
            // update SR& AR
            SR_AR_update();

        }

//        HoldQ1.add(job);
//        SR_AR_update();
//
////        ExcJob=HoldQ1.poll();
////        putInCPU(ExcJob) ;
////        
//        if (HoldQ1.isEmpty()) {
//            TQuantum = job.JobBT;
//        }
//        if (!HoldQ1.isEmpty()) {
//            TQuantum = (int) ComputeAvgBT(HoldQ1);
//            SR_AR_update();
//        }
//        //CPU executes P by TQ time
    }

    public static void HoldQ2_DP() {//Task2 done 95%
        double avgWT = 0;
        double sumWT = 0;
        Job jobDR;
        int size = HoldQ2.size();
        if (!HoldQ2.isEmpty()) {
            for (Job J : HoldQ2) {
                J.setWaitT(CurrentTime - J.getEnterQ2time());
                sumWT += J.getWaitT();

            }
            avgWT = (double) (sumWT / size);

            // update priority for old jobs only
            for (Job J : HoldQ2) {
                if ((J.getWaitT() - avgWT) > 0 && !J.isIsNew()) {
                    J.setDynamicPriority((int) (((J.getWaitT() - avgWT) * 0.2) + (J.getJobPriority() * 0.8)));
                } else if (J.isIsNew()) {
                    J.setJobPriority(J.getDynamicPriority());
                    J.setIsNew(false);
                }

            }
            SortHoldQ2();

            //move one jobs to the Ready Queue
            Object[] Queue2 = HoldQ2.toArray();
            for (int i = 0; i < Queue2.length; i++) {
                Job job1 = (Job) Queue2[i];
                Job job2 = (Job) Queue2[i + 1];
                if (job1.getDynamicPriority() > job2.getDynamicPriority()) {
//                    HoldQ2.remove(job1);
                    inHoldQ1_DRR(job1);
                    SR_AR_update();
                } else {

                    //                    HoldQ2.remove(job2);
                    inHoldQ1_DRR(job2);
                    SR_AR_update();
                }
            }

//            for (int j = 0; j < HoldQ2.size(); j++) {
//                jobDR = HoldQ2.poll();
//                if (jobDR.getJobMemS() <= AvailMemo && jobDR.getJobDevice() <= AvailDevs) {
//                    AvailMemo -= jobDR.getJobMemS();
//                    AvailMemo -= jobDR.getJobDevice();
//
//                    inHoldQ1_DRR(jobDR);
//                    SR_AR_update();
//                } else {
//                    HoldQ2.add(jobDR);
//                }
//            }
        }
    }

    public static void SortHoldQ2() {//95% done
        // Object[] jobQ2 = HoldQ2.toArray();
        Job[] jobQ2 = new Job[HoldQ2.size()];
        for (int n = 0; n < jobQ2.length; n++) {
            jobQ2[n] = HoldQ2.poll();
        }
        for (int i = 0; i < jobQ2.length; i++) {
            //   for (int n = i + 1; n < jobQ2.length; n++) {//Replaced the n with i+1
            Job temp = null;
            if (jobQ2[i].getJobPriority() < jobQ2[i + 1].getJobPriority()) {
                //swap jobs
                temp = jobQ2[i];
                jobQ2[i] = jobQ2[i + 1];
                jobQ2[i + 1] = temp;

            } else if (jobQ2[i].getJobPriority() == jobQ2[i + 1].getJobPriority()) {
                if (jobQ2[i + 1].getJobID() < jobQ2[i].getJobID()) {
                    //swap jobs
                    temp = jobQ2[i];
                    jobQ2[i] = jobQ2[i + 1];
                    jobQ2[i + 1] = temp;
                }
            }
            //  }
        }
        //clear Q2 to make sure we start fresh 
        HoldQ2.clear();

        //re fill Q2 with the jobs in order
        for (int i = 0; i < jobQ2.length; i++) {
            HoldQ2.add(jobQ2[i]);
        }

    }

    public static double ComputeAvgBT(Queue<Job> Queue) {//done
        double AvgBT = 0;
        double sumBT = 0;

        //Compute AvgBT in Q1
        for (Job job : Queue) {
            sumBT += job.JobBT;
        }
        return AvgBT = sumBT / Queue.size();
    }

    public static void Task0(Job J) {//done
//BTi is the process burst time
//AvgBT is the average burst time of all processes in Hold Queue 1
//X and Y represent available Memory and Devices, respectively.
        double BTi = J.JobBT;
        double AvgBT = 0;

        if (HoldQ1.isEmpty()) {
            AvgBT = BTi;
            HoldQ1.add(J);
            SR_AR_update();

        } else {
            AvgBT = ComputeAvgBT(HoldQ1);
        }

        if (BTi > AvgBT) {//put the process in Hold Queue 2
            HoldQ2.add(J);
            J.setEnterQ2time(CurrentTime);

            AvailMemo -= J.JobMemS;

            AvailDevs -= J.JobDevice;

        } else { //BTi < = AvgBTput .put the process in Hold Queue 1

            inHoldQ1_DRR(J);
            SR_AR_update();
            AvailMemo -= J.JobMemS;
            AvailDevs -= J.JobDevice;
        }

    }

    public static void Task1() {//98%done

        double DBTi;
        double AvgBT = 0;
        if (ExcJob.getRemBT() == 0) {
            AvgBT = ComputeAvgBT(HoldQ1);

            for (Job J : HoldQ3) {
                DBTi = J.getJobBT();
                if (J.JobMemS <= AvailMemo && J.JobDevice <= AvailDevs) {
                    if (DBTi > AvgBT) {//put the process in Hold Queue 2
                        HoldQ2.add(J);

                        AvailMemo -= J.JobMemS;

                        AvailDevs -= J.JobDevice;

                    } else { //put the process in Hold Queue 1

                        inHoldQ1_DRR(J);
                        AvailMemo -= J.JobMemS;
                        AvailDevs -= J.JobDevice;
                    }
                }

            }
        }
    }

    public static void finalDisplay(PrintWriter output) {//parameter print writer 
    }

    public static void displayEvent(PrintWriter output) {
    }

}
