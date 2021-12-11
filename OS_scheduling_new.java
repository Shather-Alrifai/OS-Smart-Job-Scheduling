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

    //Q3 ,waiting linked list
    public static LinkedList<Job> HoldQ3 = new LinkedList<Job>();

    //completed job Q linkedlist
    static LinkedList<Job> CompletedQ = new LinkedList<Job>();

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

//Main method 
    public static void main(String[] args) throws Exception {
        //create file object
        File inFile = new File("input2.txt");
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
       
        String line;
        String[] command;
        // start reading from the input file

        while (input.hasNextLine()) {
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
                        Dtime = time;

                        //----------------------------------------------------------
                        // poll out first job to be executed
                        Job first_job = AllJobs.poll();
                        // set system time = first job arraival time
                        CurrentTime = first_job.getJobArrvTime();
                        // allocate memory& devices to the job
                        // available memory = system main memory - first job requested memory
                        AvailMemo -= first_job.getJobMemS();
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
                        if (Dtime == 999999 && CompletedQ.size() == TotalJobs) {
                            //StartSystem();
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
                            Dtime = 0;
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

    public static void externalEvent() throws Exception {//100% jumana, task 0
      
        // If job ID != 0 so it is A job.    
        if (!AllJobs.isEmpty()) {
            Job J = AllJobs.poll();
            // in case of "D" job
            if (J.getJobID() == 0) {
                displayEvent(J.getJobArrvTime());
               
            } else {  // in case of "A" job
                // if there were available main memory and devices 
                // the job is sent to hold queue 1 (ready queue)
                if (J.getJobMemS() <= AvailMemo && J.getJobDevice() <= AvailDevs) {
                    AvailMemo -= J.getJobMemS();
                    AvailDevs -=  J.getJobDevice();
                    // If the hold queue 1 is empty then The AR = the first job burst time
                    if (HoldQ1.isEmpty()) {
                        HoldQ1.add(J);
                        AvgBT = J.getJobBT();
                        AR = J.getJobBT();
                        // if the hold queue 1 is not empty then we calculate the AR and compare the job with the AR to put the job either in  hold queue1 or hold queue2
                    } else {
                        ComputeAvgBT();
                        if (J.getJobBT() > AvgBT) {
                            HoldQ2.add(J);
                            J.setEnterQ2time(CurrentTime);
//                           

                        } else {
                            HoldQ1.add(J);
                            ComputeAvgBT();
                            SR_AR_update();
                          
                        }
                    }
                } else {
                    // if there were not available main memory and devices
                    // the job is sent to hold queue 3 (waiting queue)
           
                    HoldQ3.add(J);
                }
            }
        }
    }

    public static void internalEvent() {
      
        ExcJob.setRemBT(ExcJob.getRemBT() - TQuantum);

        if (ExcJob.getRemBT() <= 0) {
            // job is terminated

            Jobterminate();//correct 

            // next job is sent to CPU
            if (!HoldQ1.isEmpty()) {//this part is correct 100%
                ExcJob = HoldQ1.poll();

                // set quantum time
                TQuantum =DynamicTQuantum();
                // set the job start time of execution
                ExcJob.setJobST(CurrentTime);
                // set the executing job finish time
                int finish = Math.min(ExcJob.getRemBT(), TQuantum);
                ExcJob.setJobFT(CurrentTime + finish);
            
                // update SR& AR
                SR_AR_update();

            }
        } else {
            // if the job is not finished, it is sent to hold queue 1 (ready queue)
            DRR();
        }

    }

    public static void Jobterminate() {

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

    public static void SR_AR_update() {//1000%

        if (HoldQ1.isEmpty()) {
            AR = 0;
        } else {
            Job job = HoldQ1.peek();
            for (int k = 0; k < HoldQ1.size() && job != null; k++) {
                SR += (job.getJobWeight() * job.getRemBT());
                job = job.getNext();
            }

            AR = SR / HoldQ1.size();

        }

    }

    public static int DynamicTQuantum() {
        return Math.min(ExcJob.getRemBT(),AR );
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
            TQuantum =DynamicTQuantum();
            // send next job to CPU
            ExcJob = job;

            // set the job start time of execution
            ExcJob.setJobST(CurrentTime);
            // set the executing job finish time
            int finish = Math.min(ExcJob.getRemBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + finish);
            SR_AR_update();
            ComputeAvgBT();

        }

    }

    public static void ComputeAvgBT() {//%100
        if (!HoldQ1.isEmpty()) {
            int sumBT = 0;

            //Compute AvgBT in Q1
            for (Job job : HoldQ1) {
                sumBT += job.getJobBT();
            }
            AvgBT = sumBT / HoldQ1.size();
        }


    }

    public static void Task1() {//moveJobFromQueue3 95% jumana
        // Task 1
        // AvgBT = ComputeAvgBT();
     
        if (!HoldQ3.isEmpty()) {

            for (int i = 0; i < HoldQ3.size(); i++) {
                Job job = HoldQ3.peek();
                if (AvailMemo >= job.getJobMemS() && AvailDevs >= job.getJobDevice()) {

                    ComputeAvgBT();
                    if (HoldQ1.isEmpty()) {
                        AvgBT = job.getJobBT();
                    }

                    if ((job).getJobBT() > AvgBT) {
                        HoldQ2.add((job));
                        HoldQ3.remove((job));
                        //updating only happens when changes are happening to queue 1
                        AvailMemo -= (job).getJobMemS();
                        AvailDevs -= (job).getJobDevice();

                    } else {
                        HoldQ1.add((job));
                        HoldQ3.remove((job));
                        AvailMemo -= (job).getJobMemS();
                        AvailDevs -= (job).getJobDevice();
                        ComputeAvgBT();
                        SR_AR_update();

                    }

                }
            }


        }

    }

    public static void HoldQ2_DP() {//Task2 done 100%
        if (HoldQ2.isEmpty()) {
            return;
        }
        // Task 2
        if (!HoldQ2.isEmpty()) {
            calculate_dynamic_priority();//correct 100%

            // Sort holdQ2 
            SortHoldQ2();//correcttto

            Job job = HoldQ2.peek();

            //try to move job to Q1
            // The Job at the head is put in the Ready Queue.Q1
            HoldQ1.add(HoldQ2.poll()); /// no need to use remove since poll removes 
            ComputeAvgBT();
            SR_AR_update();

        }

    }

    public static void calculate_dynamic_priority() {//%100 correct

        double totalWait = 0;
        double dynamic_priority = 0.0;
        // Compute wait time for all Jobs in holdQ2

        Job J = HoldQ2.peek();
        for (int j = 0; j < HoldQ2.size() && J != null; j++) {

            J.setWaitT(CurrentTime - J.getJobArrvTime());
            totalWait += J.getWaitT();
            J = J.getNext();

        }

        double avgWait = totalWait / HoldQ2.size();

        J = HoldQ2.peek();
        // Set the dynamic priority for all processes.
        for (int j = 0; j < HoldQ2.size() && J != null; j++) {

            // if the job doesnt have a previous DP
            if (J.getDynamicPriority() == -1) {
                J.setDynamicPriority(J.getJobPriority());
            } else {//J.getDynamicPriority() != -1 , has already a dp but needs update

                if ((J.getWaitT() - avgWait) > 0) {

                    dynamic_priority = (J.getWaitT() - avgWait) * 0.2 + J.getDynamicPriority() * 0.8;
                    J.setDynamicPriority(dynamic_priority);
                }
            }

            J = J.getNext();
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
        Collections.sort(CompletedQ, new sortbyID());
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
        Collections.sort(CompletedQ, new sortbyID());

        for (Job J : CompletedQ) {
            J.setJobTAT(J.getJobFT() - J.getJobArrvTime());
            system_turnaround_time += J.getJobTAT();
            System.out.printf("    %-10d %-12d %-15d %-15d %n", J.getJobID(), J.getJobArrvTime(), J.getJobFT(), J.getJobTAT());
        }
        double avg_turnaround_time = system_turnaround_time / CompletedQ.size();
        System.out.printf("\n\n  Avrage System Turnaround Time =  %.3f", avg_turnaround_time);
        System.out.println("\n\n*************************\n");

    }
    //---------------------------HELPING CLASS FOR SORTING--------------------------

    static class sortbyID implements Comparator<Job> {

        // Used for sorting jobs in ascending order
        @Override
        public int compare(Job a, Job b) {
            return (int) (a.getJobID() - b.getJobID());
        }
    }
}
