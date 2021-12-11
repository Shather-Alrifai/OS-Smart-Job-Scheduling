/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os_scheduling_new;

/*
CPCS361 ,section:IAR
project: Smart process scheduling 
Group 3

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
import java.io.*;
import java.util.*;

public class OS_schedulin_project_DRR_Group3 {

    /**
     * @param args the command line arguments
     */
    public static Queue<Job> AllJobs = new LinkedList<Job>();      //all Valid jobs queue
    public static LinkedList<Job> HoldQ1 = new LinkedList<Job>();  //Shortest jobs queue(ready queue)
    public static LinkedList<Job> HoldQ2 = new LinkedList<Job>();  //longer jobs queue
    public static LinkedList<Job> HoldQ3 = new LinkedList<Job>();  //waiting queue

    static LinkedList<Job> CompletedQ = new LinkedList<Job>();   //completed jobs queue 

    static int TotalMemo = 0;//Toatl Main memory of the system
    static int AvailMemo = 0;//Available memory

    static int TotalDevs = 0;//Total devices in system
    static int AvailDevs = 0;//Available Devices 

    static int SystemStartTime = 0;//Start time of the system
    static int CurrentTime = 0;//System current time

    static int TQuantum = 0;//Time Quantum for Rounds robin
    static int TotalJobs = 0;//Number of jobs in the system
    static Job ExcJob; //Job executing in CPU

    static int i = 0;//i = arrival time of the next job
    static int e = 0; //e = time of the next internal event

    static int SR = 0; //SR weighted sum of the remaining burst times in the ready queue
    static int AR = 0; //AR weighted average of the burst times in the ready queue
    static double AvgBT = 0; //average burst times in the ready queue

    static int Dtime = 0;  //display time 
    static final int Infinity = 999999;

    static PrintWriter outputWriter;
    static Scanner input;

//Main method 
    public static void main(String[] args) throws Exception {
        //create file object
        File inputFile = new File("input2.txt");
        // check if file exists or not .
        if (!inputFile.exists()) {
            System.out.println("not exists");
            System.exit(0);
        }
        // read from input file , write in outputFile .  
        input = new Scanner(inputFile);
        File outFile = new File("output2  task 0 DD.txt");
        outputWriter = new PrintWriter(outFile);
        //------------------------------------------------------------
        String inLine;
        String[] jobCommand;

        // start reading from the input file
        while (input.hasNextLine()) {
            // read lines in sequence from the input file
            //remove unwanted charachters
            inLine = input.nextLine().replaceAll("[a-zA-Z]=", "");
            // separate the info and save it in an array
            jobCommand = inLine.split(" ");
            //------------------------------------------------------------------
            // read system configuration
            switch (jobCommand[0]) {
                //--------------------------------------------------------------
                case "C":
                    SystemStartTime = Integer.parseInt(jobCommand[1]);
                    CurrentTime = SystemStartTime;
                    TotalMemo = Integer.parseInt(jobCommand[2]);
                    AvailMemo = TotalMemo;
                    TotalDevs = Integer.parseInt(jobCommand[3]);
                    AvailDevs = TotalDevs;
                    break;
                //--------------------------------------------------------------
                // read the jobs
                case "A":
                    int ArrivTime = Integer.parseInt(jobCommand[1]);
                    int jobID = Integer.parseInt(jobCommand[2]);
                    int Req_Memo = Integer.parseInt(jobCommand[3]);
                    int Req_devics = Integer.parseInt(jobCommand[4]);
                    int burTime = Integer.parseInt(jobCommand[5]);
                    int job_Prio = Integer.parseInt(jobCommand[6]);
                    // create a Job instance for all valid jobs then add them to all AllJobs queue
                    if (Req_Memo <= TotalMemo && Req_devics <= TotalDevs) {
                        AllJobs.add(new Job(ArrivTime, jobID, Req_Memo, Req_devics, burTime, job_Prio));
                        TotalJobs++;
                    }
                    break;
                case "D":
                    //read the time of display next to D job
                    int time = Integer.parseInt(jobCommand[1]);
                    if (time != 999999) {
                        //creat a D job and add it to AllJobs queue 
                        AllJobs.add(new Job(time));
                    } else {
                        Dtime = time;

                        //----------------------------------------------------------
                        //get first job to be executed
                        Job Job_num1 = AllJobs.poll();
                        // set Current time = first job arraival time
                        CurrentTime = Job_num1.getJobArrvTime();
                        // allocate memory& devices to the job
                        AvailMemo -= Job_num1.getJobMemS();
                        AvailDevs -= Job_num1.getJobDevice();
                        // time quantum = first job burst time
                        TQuantum = Job_num1.getJobBT();
                        // assign the first job to CPU
                        ExcJob = Job_num1;
                        //set execution start time is the current time
                        ExcJob.setJobST(CurrentTime);
                        //set job execution  finish time 
                        ExcJob.setJobFT(TQuantum + CurrentTime);

                        //----------------------------------------------------------
                        // send the rest of jobs to cpu
                        //assign values to i,e 
                        while (TotalJobs != CompletedQ.size()) {
                            // i value to execute external events
                            if (AllJobs.isEmpty()) {
                                i = Infinity;
                            } else {
                                i = AllJobs.peek().getJobArrvTime();
                            }
                            // e value to execute internal events
                            if (ExcJob == null) {
                                e = Infinity;
                            } else {
                                e = ExcJob.getJobFT();
                            }
                            //------------------------------------------------------
                            // update current time in each system iteration
                            CurrentTime = Math.min(e, i);
                            //------------------------------------------------------
                            // the system will execute acccording to i and e values

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
                        // print system final state&  clear the system variables
                        if (Dtime == 999999 && CompletedQ.size() == TotalJobs) {
                            finalDisplay();
                            HoldQ1.clear();
                            HoldQ2.clear();
                            AllJobs.clear();
                            CompletedQ.clear();
                            ExcJob = null;
                            SystemStartTime = 0;
                            CurrentTime = 0;
                            TQuantum = 0;
                            TotalJobs = 0;
                            Dtime = 0;
                            AR = 0;
                            SR = 0;
                        }
                    }// end of dealing with this system configuration
                    break;
            }
        }// end of while (input.hasNextLine())
        input.close();
        outputWriter.close();

    }//end of main
 //------------------------------EXTERNAL EVENT------------------------------
    public static void externalEvent() throws Exception {
        // take from AllJobs queue 

        if (!AllJobs.isEmpty()) {
            Job J = AllJobs.poll();
            // in case of "D" job , display system at that time
            if (J.getJobID() == 0) {
                displayEvent(J.getJobArrvTime());

                // If job ID != 0 so it is an A job.   
            } else {  // in case of "A" job
                // if there were available main memory and devices 
                //allocater resources to the job
                if (J.getJobMemS() <= AvailMemo && J.getJobDevice() <= AvailDevs) {
                    AvailMemo -= J.getJobMemS();
                    AvailDevs -= J.getJobDevice();
                    //invoke task 0 to place the job in either HoldQ1 OR HoldQ2
                    Task0(J);

                } else {
                    // if there were not available main memory and devices
                    // the job is sent to hold queue 3 (waiting queue)
                    HoldQ3.add(J);
                }
            }
        }//end of  if (!AllJobs.isEmpty()).
    }
 //------------------------------INTERNAL EVENT------------------------------
   
    public static void internalEvent() {
        
      //calculating the remaining Burst time of the ExcJob currently.
        ExcJob.setRemBT(ExcJob.getRemBT() - TQuantum);

        if (ExcJob.getRemBT() <= 0) {
            // job is terminated
            
            //invoke method 
            Jobterminate(); 

            // next job is sent to CPU
            if (!HoldQ1.isEmpty()) {
                
                //remove the process at the head of the Queue
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
 //------------------------------JOB TERMINATION------------------------------
    public static void Jobterminate() {
        
        //add released resources from CPU back to available memory and devices
        AvailMemo += ExcJob.getJobMemS();
        AvailDevs += ExcJob.getJobDevice();
        
        // add the finished job to complete queue 
        CompletedQ.add(ExcJob);
        
        // no jobs in CPU
        ExcJob = null;
        
        // move processes from HQ2 to HQ1 and From HQ3 to HQ1 OR HQ2 if possible
        Task1();
        
        //invoke DP task 2
        HoldQ2_DP();
       

    }
 //------------------------------TASK 0------------------------------
    public static void Task0(Job J) {
        //running Queue is empty
        if (HoldQ1.isEmpty()) {
            //average time=job's burst time
            AvgBT = J.getJobBT();
            //AR =job's burst time
            AR = J.getJobBT();
            
            /* if the hold queue 1 is not empty:
            -then we calculate the AR  
            -compare the job with the AR 
            
            to put the job either in:  hold queue1 or hold queue2
            */
        } else {
            ComputeAvgBT();
        }
        
        //if a job's burst time> AvgBT
        if (J.getJobBT() > AvgBT) {
            
            //put in HQ2
            HoldQ2.add(J);
            J.setEnterQ2time(CurrentTime);

        } else {
            //if job's BT < AvgBT
            HoldQ1.add(J);
            ComputeAvgBT();
            SR_AR_update();

        }
    }
 //------------------------------TASK 1------------------------------
     public static void Task1() {
        // Task 1
        //move Jobs From Queue3 to Queue1 or Queue2
        if (!HoldQ3.isEmpty()) {

            for (int i = 0; i < HoldQ3.size(); i++) {
                Job job = HoldQ3.peek();
                
                //check if there is enough available resources 
                if (AvailMemo >= job.getJobMemS() && AvailDevs >= job.getJobDevice()) {

                   //if the HoldQ1 is empty assign average burts of HoldQ1 = job burst.
                   //else compute th average busrt and move job to either HoldQ1 or HoldQ2
                    if (HoldQ1.isEmpty()) {
                        AvgBT = job.getJobBT();
                    }
                     
                       ComputeAvgBT();
                    if (job.getJobBT() > AvgBT) {
                        //add job to HoldQ2
                        HoldQ2.add((job));
                        HoldQ3.remove((job));
                        //Allocate resourses to job 
                        AvailMemo -= (job).getJobMemS();
                        AvailDevs -= (job).getJobDevice();

                    } else {//Job BT() <= AvgBT
                        //add job to HoldQ2
                        HoldQ1.add((job));
                        HoldQ3.remove((job));
                        //Allocate resourses to job 
                        AvailMemo -= (job).getJobMemS();
                        AvailDevs -= (job).getJobDevice();
                        //update average burts time , SR&AR
                        ComputeAvgBT();
                        SR_AR_update();

                    }

                }
            }//end for loop

        }

    }
     
 //------------------------------ DRR ------------------------------

    public static void DRR() {
// either add the excuting job to HoldQ1 if  it needs more quantum time 
//or if the executing job is done , move the next job to execute in cpu

        if (HoldQ1.isEmpty()) {
            // if there is no jobs in hold queue 1
            // Return the executing job to cpu , it takes its full time
            TQuantum = ExcJob.getJobBT();
            //job start time of execution
            ExcJob.setJobST(CurrentTime);
            // executing job finish time
            int jobFinish_time = Math.min(ExcJob.getJobBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + jobFinish_time);
            // update SR& AR
            SR_AR_update();

        } else {

            // executing job is sent to hold queue 1 (ready queue)
            HoldQ1.add(ExcJob);
            // update SR& AR
            SR_AR_update();
            Job job = HoldQ1.poll();
            // update SR& AR
            SR_AR_update();
            //update quantum time
            TQuantum = DynamicTQuantum();
            // send next job to CPU
            ExcJob = job;

            //job start time of execution
            ExcJob.setJobST(CurrentTime);
            // executing job finish time
            int jobFinish_time = Math.min(ExcJob.getRemBT(), TQuantum);
            ExcJob.setJobFT(CurrentTime + jobFinish_time);
            //update average burts time , SR&AR
            SR_AR_update();
            ComputeAvgBT();

        }

    }
    
 //------------------------------TASK 2 HoldQ2_DP------------------------------

    public static void HoldQ2_DP() {
        //Task2 
        //move One process from Hold Queue 2 to Hold Queue 1 
        if (HoldQ2.isEmpty()) {
            return;
        }
        if (!HoldQ2.isEmpty()) {
            //calculate the Dynamic priority for all jobs in HoldQ2 
            Dynamic_Pr_Clc();

            // Sort holdQ2 based on Dynamic priority 
            SortHoldQ2();
            // get the first job and send it to HoldQ1
            HoldQ1.add(HoldQ2.poll());
            ComputeAvgBT();
            SR_AR_update();

        }

    }
 //------------------------------CALCULATE DYNAMIC PRIORITY------------------------------

    public static void Dynamic_Pr_Clc() {

        double totalWait = 0;
        double dynamic_priority = 0.0;
        Job J = HoldQ2.peek();
        // Compute wait time for all Jobs in HoldQ2
        for (int i = 0; i < HoldQ2.size() && J != null; i++) {
            J.setWaitT(CurrentTime - J.getJobArrvTime());
            totalWait += J.getWaitT();
            
        }
        // Compute average wait time
        double avgWait = totalWait / HoldQ2.size();

        J = HoldQ2.peek();
        // Set the dynamic priority for  all Jobs in HoldQ2.
        for (int i = 0; i < HoldQ2.size() && J != null; i++) {
            // if the job doesnt have a previous DP assign its DP as its priority
            if (J.getDynamicPriority() == -1) {
                J.setDynamicPriority(J.getJobPriority());
            } else {
                //job has already a DP but needs updating
                if ((J.getWaitT() - avgWait) > 0) {
                    dynamic_priority = (J.getWaitT() - avgWait) * 0.2 + J.getDynamicPriority() * 0.8;
                    J.setDynamicPriority(dynamic_priority);
                }
            }

        }//end of for loop
    }
 //------------------------------SORT HOLDQ2------------------------------

    public static void SortHoldQ2() {
        // sort HoldQ2 based on DP
        // convert HoldQ2 to an array of objects to perform sorting

        Object[] Q2_array = HoldQ2.toArray();
        Job temp;
        //empty the HoldQ2 to fill it after the sorting 
        HoldQ2.clear();
        //sort 
        for (int i = 0; i < Q2_array.length; i++) {
            for (int R = i + 1; R < Q2_array.length; R++) {
                if (((Job) Q2_array[i]).getDynamicPriority() < ((Job) Q2_array[R]).getDynamicPriority()) {
                    //swap
                    temp = ((Job) Q2_array[i]);
                    Q2_array[i] = ((Job) Q2_array[R]);
                    Q2_array[R] = temp;
                } else if (((Job) Q2_array[i]).getDynamicPriority() == ((Job) Q2_array[R]).getDynamicPriority()) {
                    if (((Job) Q2_array[i]).getJobArrvTime() > ((Job) Q2_array[R]).getJobArrvTime()) {
                        //swap
                        temp = ((Job) Q2_array[i]);
                        Q2_array[i] = ((Job) Q2_array[R]);
                        Q2_array[R] = temp;
                    }
                }
            }//end of for loop
        }
        // refill  HoldQ2 in order 
        for (int i = 0; i < Q2_array.length; i++) {
            HoldQ2.add((Job) Q2_array[i]);
        }

    }
 //------------------------------SR & AR UPDATE------------------------------

    public static void SR_AR_update() {
        //Update SR & AR
        if (HoldQ1.isEmpty()) {
            AR = 0;
        } else {
            Job job = HoldQ1.peek();
            for (int i = 0; i < HoldQ1.size() && job != null; i++) {
                SR += (job.getJobWeight() * job.getRemBT());
                
            }

            AR = SR / HoldQ1.size();

        }

    }
 //------------------------------DYNAMIC QUANTUM TIME------------------------------

    public static int DynamicTQuantum() {
        //calculate the dynamic Time Quantum
        return Math.min(ExcJob.getRemBT(), AR);
    }
 //------------------------------COMPUTE AVERAGE BURST TIME------------------------------

    public static void ComputeAvgBT() {
        // calculate the average burst time of jobs in HoldQ1
        if (!HoldQ1.isEmpty()) {
            int sumBT = 0;
            for (Job job : HoldQ1) {
                sumBT += job.getJobBT();
            }
            AvgBT = sumBT / HoldQ1.size();
        }

    }
 //------------------------------DISPLAY EVENT------------------------------

    public static void displayEvent(int DArrvTime) {
        // display event current state acording to display time

        outputWriter.println("\n<< At time " + DArrvTime + ":");
        outputWriter.println("  Current Available Main Memory = " + AvailMemo);
        outputWriter.println("  Current Devices               = " + AvailDevs);
        outputWriter.println("\n  Completed jobs: \n  ----------------");
        outputWriter.println("  Job ID   Arrival Time    Finish Time  Turnaround Time \n"
                + "  =================================================================");
        //sort completedQ based on job ID
        Collections.sort(CompletedQ, new sortbyID());
        for (Job J : CompletedQ) {
            J.setJobTAT(J.getJobFT() - J.getJobArrvTime());
            outputWriter.printf("    %-10d %-12d %-15d %-15d %n", J.getJobID(), J.getJobArrvTime(), J.getJobFT(), J.getJobTAT());
        }

        outputWriter.println();
        //print the state of HoldQ3
        outputWriter.println("\n\n  Hold Queue 3: \n  ----------------");
        for (Job J : HoldQ3) {
            outputWriter.printf("%6d", J.getJobID());
        }
        //print the state of HoldQ2
        outputWriter.println("\n\n  Hold Queue 2: \n  ----------------");
        for (Job J : HoldQ2) {
            outputWriter.printf("%6d", J.getJobID());
        }
        //print the state of HoldQ1 
        outputWriter.println("\n\n  Hold Queue1 (Ready Queue): \n  ----------------");
        outputWriter.println("  JobID    NeedTime    Total Execution Time \n"
                + "  ===============================");
        //set Execution time for all jobs in ready queue
        for (Job J : HoldQ1) {
            J.setExecution_time(J.getJobBT() - J.getRemBT());
            outputWriter.printf("%5d%10d%15d\n\n", J.getJobID(), J.getJobBT(), J.getExecution_time());
        }
        //print cpu status
        outputWriter.println("\n\n  Process running on the CPU: \n  ----------------------------");
        outputWriter.println("  Job ID   NeedTime    Total Execution Time");
        if (ExcJob != null) {
  //set Execution time executing job in cpu
            ExcJob.setExecution_time(ExcJob.getJobBT() - ExcJob.getRemBT());
            outputWriter.printf("%5d%10d%15d\n\n\n", ExcJob.getJobID(), ExcJob.getJobBT(), ExcJob.getExecution_time());
        }

    }
 //------------------------------FINAL DISPALY------------------------------

    public static void finalDisplay() {
        outputWriter.println("<< Final state of system: ");
        outputWriter.println("  Current Available Main Memory = " + AvailMemo);
        outputWriter.println("  Current Devices               = " + AvailDevs + "\n");
        outputWriter.println("  Completed jobs: ");
        outputWriter.println("  ----------------");
        outputWriter.println("  Job ID   Arrival Time    Finish Time  Turnaround Time ");
        outputWriter.println("  =================================================================");
        double system_turnaround_time = 0;
 
        //sort completedQ based on job ID
        Collections.sort(CompletedQ, new sortbyID());
       //set the turnaround time for all jobs in Completed queue.
       //compute the total system turnaround time
        for (Job J : CompletedQ) {
            J.setJobTAT(J.getJobFT() - J.getJobArrvTime());
            system_turnaround_time += J.getJobTAT();
            outputWriter.printf("    %-10d %-12d %-15d %-15d %n", J.getJobID(), J.getJobArrvTime(), J.getJobFT(), J.getJobTAT());
        }
        //compute the average system turnaround time
        double avg_turnaround_time = system_turnaround_time / CompletedQ.size();
        outputWriter.printf("\n\n  Avrage System Turnaround Time =  %.3f", avg_turnaround_time);
        outputWriter.println("\n\n*********\n");

    }
    
//------------------------------SORT BY ID CLASS ------------------------------
    static class sortbyID implements Comparator<Job> {

        // Used for sorting jobs in ascending order based on ID 
        @Override
        public int compare(Job a, Job b) {
            return (int) (a.getJobID() - b.getJobID());
        }
    }
}
