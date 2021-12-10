/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os_scheduling_new;

/**
 *
 * @author best2
 */
import java.io.*;
import java.util.*;

public class OS_scheduling_new {

    /**
     * @param args the command line arguments
     */
    //all job Q
    public static Queue<Job> AllJobs = new LinkedList<Job>();
    //Q1 ,shortest priorityQ
    public static LinkedList<Job> HoldQ1 = new LinkedList<Job>();
    //Q2 ,longer jobs Q
    public static LinkedList<Job> HoldQ2 = new LinkedList<Job>();
//    public static Queue<Job> HoldQ2 = new LinkedList<Job>(new Comparator<Job>() {
//        @Override
//        public int compare(Job J1, Job J2) {
//            if (J1.getDynamicPriority() > J1.getDynamicPriority() ) {
//                return -1;
//            } else if (J1.getDynamicPriority() == J2.getDynamicPriority()) {
//                return 0;
//            } else {
//                return 1;
//            }
//        }
//
//    });
    //Q3 ,waiting linked list
    public static LinkedList<Job> HoldQ3 = new LinkedList<Job>();

    //completed job Q linkedlist
    public static LinkedList<Job> CompletedQ = new LinkedList<Job>();

    static int TotalMemo = 0;//Main memory of the system
    static int AvailMemo = 0;//remaining Available memory

    static int TotalDevs = 0;//All devices in system
    static int AvailDevs = 0;//Available Devices in sysytem

    static int SystemStartTime = 0;//System start time of the system
    static int CurrentTime = 0;//System current time

    static int TQuantum = 0;//Time Quantum of Rounds robin,must be calculated
    static int TotalJobs = 0;//number of jobs read from input file
    static Job ExcJob; //job executing in CPU

    //i
    static int i = 0;
    //e
    static int e = 0;

    static int SR = 0;   //SR weighted sum of the remaining burst times in the ready queue
    static int AR = 0;//AR weighted average of the burst times

    static int Dtime = 0;
    //  static int DisplayTime = 0;
    static final int Infinity = 999999;
    static PrintWriter output;
    static Scanner input;
    static double AvgBT = 0;

    // For while loop that reads system configurations from the input files.
//    private static boolean system_running = true;
//    // For while loop that deals with each system configuration separately.
//    private static boolean new_system = true;
//    // To determine the job in the CPU will terminate by quantum or by burst time of the job itself.
//    private static int terminate_by_quantum = 0;
//    private static int terminate_by_burst = 0;
//
//    private static int AT_process_at_beginnig = -1;
//    private static int finish_time_CPU = -1;
//Main method 
    public static void main(String[] args) throws Exception {
        //create file object
        File inFile = new File("input1.txt");
        if (!inFile.exists()) {
            System.out.println("not exists");
            System.exit(0);
        }// check if file exists or not .
        // read from input file , write in outputFile .  
        input = new Scanner(inFile);
        File outFile = new File("outputFile.txt");
        output = new PrintWriter(outFile);
        //------------------------------------------------------------

        // Read one system configuration.
        // readFile();
        String line;
        String[] command;
        // start reading from the input file

        while (input.hasNext()) {
            // read line by line from the input file
            line = input.nextLine().replaceAll("[a-zA-Z]=", "");
            // separate the info in an array
            command = line.split(" ");
            //------------------------------------------------------------------
            // read system configuration
            switch (command[0]) {
                //--------------------------------------------------------------
                case "C":
                    SystemStartTime = Integer.parseInt(command[1]);
                    CurrentTime = SystemStartTime;
                    TotalMemo = Integer.parseInt(command[2]);
                    AvailMemo = TotalMemo;
                    TotalDevs = Integer.parseInt(command[3]);
                    AvailDevs = TotalDevs;
                    break;
                //--------------------------------------------------------------
                case "A":
                    int arrival_time = Integer.parseInt(command[1]);
                    int job_id = Integer.parseInt(command[2]);
                    int requested_mm = Integer.parseInt(command[3]);
                    int requested_d = Integer.parseInt(command[4]);
                    int burst_time = Integer.parseInt(command[5]);
                    int job_priority = Integer.parseInt(command[6]);
                    // create process for all valid jobs then add them to all_jobs queue
                    if (requested_mm <= TotalMemo && requested_d <= TotalDevs) {
                        AllJobs.add(new Job(arrival_time, job_id, requested_mm, requested_d, burst_time, job_priority));
                        TotalJobs++;
                    }
                    break;
                case "D":
                    int time = Integer.parseInt(command[1]);
                    if (time != 999999) {
                        // print the state of the system at a specified time
                        AllJobs.add(new Job(time));
                    } else {
                        //----------------------------------------------------------
                        // poll out first job to be executed
                        Job first_job = AllJobs.poll();
                        // set system time = first job arraival time
                        CurrentTime = first_job.getJobArrvTime();
                        // allocate memory& devices to the job
                        // available memory = system main memory - first job requested memory
                        AvailMemo -= first_job.getJobDevice();
                        // available devices = system serial devices - first job requested devices
                        AvailDevs -= first_job.getJobDevice();
                        // set quantum = first job burst time
                        TQuantum = first_job.getJobBT();
                        // send first job to CPU
                        ExcJob = first_job;
                        // set the job start time of execution
                        ExcJob.setJobST(CurrentTime);
                        // set the executing job finish time
                        ExcJob.setJobFT(TQuantum + CurrentTime);
                        SR_AR_update();
                        //----------------------------------------------------------
                        // send the rest of jobs to cpu
                        while (TotalJobs != CompletedQ.size()) {
                            // set i value to perform internal events
                            if (AllJobs.isEmpty()) {
                                i = Infinity;
                            } else {
                                i = AllJobs.peek().getJobArrvTime();
                            }
                            // set e value to perform external events
                            if (ExcJob == null) {
                                e = Infinity;
                            } else {
                                e = ExcJob.getJobFT();
                            }
                            //------------------------------------------------------
                            // update system time each iteration
                            CurrentTime = Math.min(e, i);
                            //------------------------------------------------------
                            // the system works acccording to i and e values
                            // System.out.println(exe_job + "i=" + i + "e=" + e);
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
                        } // end of while loop
                        //----------------------------------------------------------
                        // print system final state& reset variables
                        if (time == 999999 && CompletedQ.size() == TotalJobs) {
                            finalDisplay();
                            CompletedQ.clear();
                            HoldQ1.clear();
                            HoldQ2.clear();
                            AllJobs.clear();
                            ExcJob = null;
                            SystemStartTime = 0;
                            CurrentTime = 0;
                            TQuantum = 0;
                            TotalJobs = 0;
                            time = 0;
                            SR = 0;
                            AR = 0;
                        }
                    }// end of dealing with this system configuration
                    break;
            }
        }// end of while loop that reads from input file
        input.close();
        output.close();

    }//end of main

    public static void readFile() throws Exception {
        String inputLn;
        String[] command;
        Job job;

        while (input.hasNextLine()) {
            // read line by line from the input file
            inputLn = input.nextLine().replaceAll("[a-zA-Z]=", "");
            System.out.println("input line: " + inputLn);
            // separate the info in an array
            command = inputLn.split(" ");

            //print the command for trace
            for (int i = 0; i < command.length; i++) {
                System.out.print("attribute:  " + command[i] + " ");
                System.out.println("");
            }
            //------------------------------------------------------------------
            // read system configuration   C 0 M=120 S=0

            if (command[0].equalsIgnoreCase("C")) {
                SystemStartTime = Integer.parseInt(command[1]);
                //  System.out.println(command[1]);
                CurrentTime = SystemStartTime;
                TotalMemo = Integer.parseInt(command[2]);
                AvailMemo = TotalMemo;
                //   System.out.println(command[2]);
                TotalDevs = Integer.parseInt(command[3]);
                //  System.out.println(command[3]);
                AvailDevs = TotalDevs;
//--------------------------------------------------------------
                // read "A" jobs
            } else if (command[0].equalsIgnoreCase("A")) {
                int ArrvTime = Integer.parseInt(command[1]);
                int JobID = Integer.parseInt(command[2]);
                int JobMemS = Integer.parseInt(command[3]);
                int JobDevice = Integer.parseInt(command[4]);
                int JobBT = Integer.parseInt(command[5]);
                int JobPriority = Integer.parseInt(command[6]);
                // create process for all valid jobs (If there is enough total memory or devices )then add them to all_jobs queue
                if (JobMemS <= TotalMemo && JobDevice <= TotalDevs) {
                    // create new job object 
                    job = new Job(ArrvTime, JobID, JobMemS, JobDevice, JobBT, JobPriority);
                    System.out.println("job A : ");
                    AllJobs.add(job);
                    TotalJobs++;//to count the number of job entered to the queue

                }
                ////If there is not enough resources in the system for the job, the job is rejected 
            } // read "D" job
            else if (command[0].equalsIgnoreCase("D")) {
                System.out.println("D job ");
                int time = Integer.parseInt(command[1]);
                if (time != Infinity) {
                    AllJobs.add(new Job(time));
                } else {//Dtime == Infinity
                    Dtime = time;
                    StartSystem();
                    if (Dtime == Infinity && CompletedQ.size() == TotalJobs) {
                        finalDisplay();
                        //  clear system configs
                        CompletedQ.clear();

                        HoldQ1.clear();
                        HoldQ2.clear();
                        AllJobs.clear();
                        ExcJob = null;
                        SystemStartTime = 0;
                        CurrentTime = 0;
                        TQuantum = 0;
                        TotalJobs = 0;
                        Dtime = 0;
                        SR = 0;
                        AR = 0;
                    }

                }

            }

        }// end of while(input.hasNextLine())

    }

    public static void StartSystem() throws Exception {

        if (!AllJobs.isEmpty()) {
            Job firstJob = AllJobs.poll();
            // set system time = first job arraival time
            CurrentTime = firstJob.getJobArrvTime();
            // allocate memory& devices to the job
            // available memory = system main memory - first job requested memory
            AvailMemo = TotalMemo - firstJob.getJobMemS();
            // available devices = system serial devices - first job requested devices
            AvailDevs = TotalDevs - firstJob.getJobDevice();
            // set quantum = first job burst time
            TQuantum = firstJob.getJobBT();
            // Assing the first job to execute
            ExcJob = firstJob;

            //   set the job start time of execution
            firstJob.setJobST(CurrentTime);
            // set the executing job finish time
            firstJob.setJobFT(TQuantum + CurrentTime);
        }

        //       
        //   get I,E and compare >internal vs external 
        while (CompletedQ.size() != TotalJobs) {// infinte loop in i,e
            if (AllJobs.isEmpty()) {
                i = Infinity;
            } else {
                i = AllJobs.peek().getJobArrvTime();
            }
            //print i for tracing 
            //  System.out.println("i = " + i);

            if (ExcJob == null) {
                e = Infinity;
            } else {
                e = ExcJob.getJobFT();
            }
            //print e for tracing 
            //System.out.println("e = " + e);

            //The "inner loop" of your program should calculate the time of the next event,
            //set the current time as the min(i,e)??
            CurrentTime = Math.min(e, i);

            if (i < e) {
                //invoke external 
                externalEvent();
            } else if (i > e) {
                //invoke internal 
                internalEvent();
            } else {//i=e
                //do internal then external
                internalEvent();
                externalEvent();
            }

        }//end of  while (CompletedQ.size() != TotalJobs)

    }

    public static void externalEvent() throws Exception {//100% jumana, task 0
        // If job ID != 0 so it is A job.    
        if (!AllJobs.isEmpty()) {
            Job J = AllJobs.poll();
            // in case of "D" job
            if (J.getJobID() == 0) {
                displayEvent(J.getJobArrvTime());
                //task 0??
            } else if (HoldQ1.isEmpty()) {//its an A job
                AvgBT = J.getJobBT();
                HoldQ1.add(J);

                SR_AR_update();
                AvailMemo -= J.getJobMemS();
                AvailDevs -= J.getJobDevice();
                //TQuantum = DynamicTQuantum();
                AvgBT = ComputeAvgBT();

            } else if (J.getJobMemS() <= AvailMemo && J.getJobDevice() <= AvailDevs) {// If there is enough main memory and devices for the job
                AvgBT = ComputeAvgBT();
                //invoke task 0
                //  Task0();
                if (J.getJobBT() < AvgBT) {
                    HoldQ1.add(J);
                    AvailMemo -= J.getJobMemS();
                    AvailDevs -= J.getJobDevice();
                    SR_AR_update();
                    //TQuantum = DynamicTQuantum();
                    AvgBT = ComputeAvgBT();
                } else {
                    HoldQ2.add(J);
                    J.setEnterQ2time(CurrentTime);
                }

            } else {
                HoldQ3.add(AllJobs.poll());
            }
        }
    }

    public static void Task0() {//remove it
//BTi is the process burst time
//AvgBT is the average burst time of all processes in Hold Queue 1
//X and Y represent available Memory and Devices, respectively.
        System.out.println("    task 0 broooo ");
        Job J = AllJobs.poll();

        if (HoldQ1.isEmpty()) {
            AvgBT = J.getJobBT();
            HoldQ1.add(J);

            SR_AR_update();
            AvailMemo -= J.getJobMemS();
            AvailDevs -= J.getJobDevice();
            TQuantum = DynamicTQuantum();
        } else {
            AvgBT = ComputeAvgBT();

            if (J.getJobBT() > AvgBT) {//put the process in Hold Queue 2
                HoldQ2.add(J);
                J.setEnterQ2time(CurrentTime);

//                AvailMemo -= J.getJobMemS();
//                AvailDevs -= J.getJobDevice();
            } else { //BTi < = AvgBTput .put the process in Hold Queue 1

                HoldQ1.add(J);

                AvailMemo -= J.getJobMemS();
                AvailDevs -= J.getJobDevice();
                SR_AR_update();
                TQuantum = DynamicTQuantum();

            }
        }

    }

    public static void internalEvent() {
        // work on CPU
        // calcualte the remaining burst time after execution
        ExcJob.setRemBT(ExcJob.getRemBT() - TQuantum); //NULL POINTER
        // if job burst time is done
        if (ExcJob.getRemBT() <= 0) {
            // job is terminated

            Jobterminate();//correct 

            // next job is sent to CPU
            if (!HoldQ1.isEmpty()) {//this part is correct 100%
                ExcJob = HoldQ1.poll();
                // update SR& AR
                SR_AR_update();
                // set quantum time
                TQuantum = DynamicTQuantum();
                // set the job start time of execution
                ExcJob.setJobST(CurrentTime);
                // set the executing job finish time
                int finish = Math.min(ExcJob.getRemBT(), TQuantum);
                ExcJob.setJobFT(CurrentTime + finish);
                AvgBT = ComputeAvgBT();
            }
        } else {
            // if the job is not finished, it is sent to hold queue 1 (ready queue)
            DRR();
        }

    }

    public static void Jobterminate() {

        if (ExcJob != null) {
            AvailMemo += ExcJob.getJobMemS();
            AvailDevs += ExcJob.getJobDevice();
            // add the finished job to complete queue 
            CompletedQ.add(ExcJob);
            // no jobs in CPU
            ExcJob = null;

            // move processes from HQ2 to HQ1 and From HQ3 to HQ1 OR HQ2 if possible
            // task 1
            Task1();//correct
            //invoke DP task 2
            HoldQ2_DP();//correct
        }

    }

    public static void SR_AR_update() {//1000%

        if (HoldQ1.isEmpty()) {
            AR = 0;
        } else {
            SR = 0;
            for (Job j : HoldQ1) {
                SR += (j.getJobWeight() * j.getRemBT());
            }

            AR = (int) (SR / HoldQ1.size());

        }

    }

    public static int DynamicTQuantum() {
        return Math.min(AR, ExcJob.getRemBT());
    }

    public static void DRR() {//95% correctooo
//invkoed when a job terminates to either add the excuting to q1 since it needs more time or move the next job to execute 

        if (HoldQ1.isEmpty()) {
            // if there is no jobs in hold queue 1
            // the current executing job takes its time to finish
            TQuantum = ExcJob.getJobBT();
            // set the job start time of execution
            ExcJob.setJobST(CurrentTime);
            // set the executing job finish time
            int finish = Math.min(TQuantum = ExcJob.getJobBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + finish);
            // update SR& AR
            SR_AR_update();

        } else {//correct

            // executing job is sent to hold queue 1 (ready queue)
            HoldQ1.add(ExcJob);
            // update SR& AR
            SR_AR_update();
            Job job = HoldQ1.poll();
            // update SR& AR
            SR_AR_update();
            //update quantum time
            TQuantum = Math.min(job.getJobBT(), AR);
            // send next job to CPU
            ExcJob = job;

            // set the job start time of execution
            ExcJob.setJobST(CurrentTime);
            // set the executing job finish time
            int finish = Math.min(ExcJob.getRemBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + finish);
            SR_AR_update();
            AvgBT = ComputeAvgBT();

        }

    }

    public static double ComputeAvgBT() {//%100
        double AvgBT = 0;
        double sumBT = 0;

        //Compute AvgBT in Q1
        for (Job job : HoldQ1) {
            sumBT += job.getJobBT();
        }
        return AvgBT = sumBT / (double) HoldQ1.size();
    }

    public static void Task1() {//moveJobFromQueue3 95% jumana
        // Task 1
        // AvgBT = ComputeAvgBT();
        //  for (int i = 0; i < HoldQ3.size(); i++) {//can be for each loop
        if (!HoldQ3.isEmpty()) {

            Object[] queue3 = HoldQ3.toArray();
            for (int i = 0; i < queue3.length; i++) {
                Job job = (Job) queue3[i];
                if (AvailMemo >= job.getJobMemS()
                        && AvailDevs >= job.getJobMemS()) {
                    AvgBT = ComputeAvgBT();
                   
                    if (job.getJobBT() <= AvgBT) {
                        HoldQ3.remove(job);
                        HoldQ1.add(job);
                        SR_AR_update();

                        AvgBT = ComputeAvgBT();
                        AvailMemo -= job.getJobMemS();
                        AvailDevs -= job.getJobDevice();

                    } else {
                        HoldQ2.add(job);
                        HoldQ3.remove(job);
                        job.setEnterQ2time(CurrentTime);
                    }
                }
            }
        }
            
    }

    public static void HoldQ2_DP() {//Task2 done 100%

        // Task 2
        if (!HoldQ2.isEmpty()) {
            calculate_dynamic_priority();//correct 100%

            // Sort holdQ2 
            SortHoldQ2();//correcttto

            Job job = HoldQ2.peek();
            System.out.println("currentTime: " + CurrentTime + "jobID: " + job.getJobID() + "DynamicPriority: " + job.getDynamicPriority());

            //try to move job to Q1
            if (job.getJobMemS() <= AvailMemo && job.getJobDevice() <= AvailDevs) {
                AvailMemo -= job.getJobMemS();
                AvailDevs -= job.getJobDevice();

                // The Job at the head is put in the Ready Queue.Q1
                HoldQ1.add(HoldQ2.poll()); /// no need to use remove since poll removes 
                SR_AR_update();

            }

        }

    }

    public static void calculate_dynamic_priority() {//%100 correct

        double totalWait = 0;
        double dynamic_priority = 0.0;
        // Compute wait time for all Jobs in holdQ2
        for (Job J : HoldQ2) {
            J.setWaitT(CurrentTime - J.getJobArrvTime());
            totalWait += J.getWaitT();
        }

        double avgWait = totalWait / HoldQ2.size();

        // Set the dynamic priority for all processes.
        for (Job J : HoldQ2) {

            // if the job doesnt have a previous DP
            if (J.getDynamicPriority() == -1) {
                J.setDynamicPriority(J.getJobPriority());
            } else {//J.getDynamicPriority() != -1 , has already a dp but needs update

                if ((J.getWaitT() - avgWait) > 0) {

                    dynamic_priority = (J.getWaitT() - avgWait) * 0.2 + J.getDynamicPriority() * 0.8;
                    J.setDynamicPriority(dynamic_priority);
                }
            }
        }
    }

    public static void SortHoldQ2() {// 100% correct 
        Object[] jobQ2 = HoldQ2.toArray();
        Job temp;
        HoldQ2.clear();
        for (int i = 0; i < jobQ2.length; i++) {
            for (int k = i + 1; k < jobQ2.length; k++) {
                if (((Job) jobQ2[i]).getDynamicPriority() < ((Job) jobQ2[k]).getDynamicPriority()) {
                    temp = ((Job) jobQ2[i]);
                    jobQ2[i] = ((Job) jobQ2[k]);
                    jobQ2[k] = temp;
                } else if (((Job) jobQ2[i]).getDynamicPriority() == ((Job) jobQ2[k]).getDynamicPriority()) {
                    if (((Job) jobQ2[i]).getJobArrvTime() > ((Job) jobQ2[k]).getJobArrvTime()) {
                        temp = ((Job) jobQ2[i]);
                        jobQ2[i] = ((Job) jobQ2[k]);
                        jobQ2[k] = temp;
                    }
                }
            }
        }
        // return array to the hold_queue2
        for (int i = 0; i < jobQ2.length; i++) {
            HoldQ2.add((Job) jobQ2[i]);
        }

    }

    public static void displayEvent(int DArrvTime) {
        //sort completed Q??

        System.out.println("\n<< At time " + DArrvTime + ":");
        System.out.println("  Current Available Main Memory = " + AvailMemo);
        System.out.println("  Current Devices               = " + AvailDevs);
        System.out.println("\n  Completed jobs: \n  ----------------");
        System.out.println("  Job ID   Arrival Time    Finish Time  Turnaround Time \n"
                + "  =================================================================");
        //    Collections.sort(CompletedQ, new sortbyID());
        for (Job J : CompletedQ) {
            J.setJobTAT(J.getJobFT() - J.getJobArrvTime());
            System.out.printf("    %-10d %-12d %-15d %-15d %n", J.getJobID(), J.getJobArrvTime(), J.getJobFT(), J.getJobTAT());
        }

        System.out.println();

        System.out.println("\n\n  Hold Queue 3: \n  ----------------");
        for (Job J : HoldQ3) {
            System.out.printf("%6d", J.getJobID());
        }

        System.out.println("\n\n  Hold Queue 2: \n  ----------------");
        for (Job J : HoldQ2) {
            System.out.printf("%6d", J.getJobID());
        }

        System.out.println("\n\n  Hold Queue1 (Ready Queue): \n  ----------------");
        System.out.println("  JobID    NeedTime    Total Execution Time \n"
                + "  ===============================");
        for (Job J : HoldQ1) {
            //set job execution time...
            J.setExecution_time(J.getJobBT() - J.getRemBT());
            System.out.printf("%5d%10d%15d\n\n", J.getJobID(), J.getJobBT(), J.getExecution_time());

        }

        System.out.println("\n\n  Process running on the CPU: \n  ----------------------------");
        System.out.println("  Job ID   NeedTime    Total Execution Time");
        if (ExcJob != null) {
            //check  Total Execution Time calc...
            ExcJob.setExecution_time(ExcJob.getJobBT() - ExcJob.getRemBT());
            System.out.printf("%5d%10d%15d\n\n\n", ExcJob.getJobID(), ExcJob.getJobBT(), ExcJob.getExecution_time());
        }

    }

    public static void finalDisplay() {
        //sort completed?
        System.out.println("<< Final state of system: ");
        System.out.println("  Current Available Main Memory = " + AvailMemo);
        System.out.println("  Current Devices               = " + AvailDevs + "\n");
        System.out.println("  Completed jobs: ");
        System.out.println("  ----------------");
        System.out.println("  Job ID   Arrival Time    Finish Time  Turnaround Time ");
        System.out.println("  =================================================================");
        double system_turnaround_time = 0;
        //  Collections.sort(CompletedQ, new sortbyID());

        for (Job J : CompletedQ) {
            J.setJobTAT(J.getJobFT() - J.getJobArrvTime());
            system_turnaround_time += J.getJobTAT();
            System.out.printf("    %-10d %-12d %-15d %-15d %n", J.getJobID(), J.getJobArrvTime(), J.getJobFT(), J.getJobTAT());
        }
        double avg_turnaround_time = system_turnaround_time / CompletedQ.size();
        System.out.printf("\n\n  Avrage System Turnaround Time =  %.3f", avg_turnaround_time);
        System.out.println("\n\n*********************************************************************\n");

    }
    //---------------------------HELPING CLASS FOR SORTING--------------------------

    class sortbyID implements Comparator<Job> {

        // Used for sorting jobs in ascending order
        @Override
        public int compare(Job a, Job b) {
            return (int) (a.getJobID() - b.getJobID());
        }

    }
}
