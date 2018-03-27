import java.io.*;
import java.util.*;

public class Manager {
	
	static int no_of_tasks=0;
	static int no_of_resource_types=0;
	static int[] resource_units=null;
	static int[] accepted_resource_units=null;
 	static Task[] tasks=null;
	static ArrayList<Task> dummy_tasks=null;
	static ArrayList<Task> blocked_tasks=null;
	static ArrayList<Task> running_tasks=null;
	
	// Main method
	public static void main(String[] args) {
	
		String file_name="";
		int flag=0;
		// getting file name & generating errors/warnings if necessary.
		switch(args.length) {
			case(0):
				System.out.println("Error -->  Filename not supplied. Rerun with filename.");
				flag=1;
				break;
			case(1):
				file_name = args[0];
				flag=1;
				break;
			default:
				file_name = args[0];
				System.out.println("Warning --> Multiple arguments supplied. Only 1 expected. First argument considered to be the file name. Others ignored.");
				flag=1;
				break;
		}
		
		if (flag==1) 
		{	read_file(file_name);
			fifo_manager(); //running fifo manager on the input
		}
		
		printing("FIFO");
		
		if (flag==1) 
		{read_file(file_name);//again reading the file since the previous variables have been manipulated
		banker_manager();//running banker manager on the input
		}
		
		printing("BANKER's");
		
	}
	//function for printing tasks with their wait times etc.
	public static void printing(String str)
	{	System.out.println("");
		System.out.println(str);
		int tt=0,tw=0;
		for(int i=0;i<no_of_tasks;i++)
		{	tasks[i].display();
			if(!tasks[i].state[1])
				{
				tt=tt+tasks[i].total_time;
				tw=tw+tasks[i].wait;
				}
		}
		float aw=((float)tw)/tt*100;
		System.out.print("Total");
		System.out.printf("	%d	%d	%.0f%%",tt,tw,aw);
		System.out.println("");
	}
	//Runs bankers' algorithm on the input
	public static void banker_manager()
	{
		int cycle=0;
		Task current_task_banker=null;
		dummy_tasks=new ArrayList<Task>();
		blocked_tasks=new ArrayList<Task>();
		running_tasks=new ArrayList<Task>();
		for(int i=0;i<no_of_tasks;i++)	
			dummy_tasks.add(tasks[i]);
		while((dummy_tasks.size()>0))//keep running until all tasks have been executed
		{
			for(int i=0;i<dummy_tasks.size();i++) //take one activity of each task
			{
				current_task_banker=dummy_tasks.get(i);
				String act_type1=current_task_banker.current_act.act_type;
				if(act_type1.equals("initiate"))
					initiate(current_task_banker,0);
				else
					if(act_type1.equals("request"))
						request_banker(current_task_banker);
					else
						if(act_type1.equals("release"))
							release(current_task_banker);
						else	
							if(act_type1.equals("terminate"))
								terminate(current_task_banker,cycle);
			}
			//Check for deadlock,if no task has been aborted previously then it means a deadlock has occured. which cannot be possible in bankers. if aborted then take back all resources.
			if((blocked_tasks.size()>0) && (running_tasks.size()==0))
			{
				boolean aborted=false;
				for(Task temp: blocked_tasks)
				{
					if(temp.state[1]==true)
					{
						aborted=true;
						for (int i = 0; i < no_of_resource_types; i++) 
							resource_units[i] += temp.res_allocated[i];
						blocked_tasks.remove(temp);
						break;
					}
				}
				
				if(!aborted)
					deadlock();
			}
			
			int i=0; //adding all resources released back to the bank
			while(i<no_of_resource_types)
			{
				resource_units[i]=resource_units[i]+accepted_resource_units[i];
				accepted_resource_units[i]=0;
				i++;
				
			}
			//adding blocked tasks before the ongoing tasks, clearing the buffers.
			dummy_tasks.clear();
			dummy_tasks.addAll(blocked_tasks);
			dummy_tasks.addAll(running_tasks);
			blocked_tasks.clear();
			running_tasks.clear();
			
		cycle++;
			
		}
	
	}
	//checks if the state upon resource grant will be a safe state
	public static boolean safestates(Task t)
	{	int res=t.current_act.res_type;
		int[] resource_units_copy=new int[no_of_resource_types];
		boolean safe=true;
		boolean not_deadlocked=false;
		Task thetask=new Task(t.id,no_of_resource_types); //creating a copy of the current task
		thetask.res_needed=t.res_needed;
		thetask.res_allocated=t.res_allocated;
		thetask.current_act=t.current_act;
		thetask.current_delay=t.current_delay;
		thetask.activities=t.activities;
		thetask.state=t.state;
		ArrayList<Task> taskscopy=new ArrayList<Task>();
		for(Task temp:dummy_tasks) //creating a copy of tasks
				taskscopy.add(temp);
			
		for(int i=0;i<no_of_resource_types;i++) //creating a copy of resources
			resource_units_copy[i]=resource_units[i];
		
		
		thetask.res_allocated[res]+=t.current_act.res_value; //simulates allocating resources to the task
		thetask.res_needed[res]-=t.current_act.res_value;
		resource_units_copy[res]-=t.current_act.res_value;
		//state is safe if all tasks terminate meaning taskscopy is empty
		while(safe==true)
		{
			safe=false;
			for(int j=0;j<taskscopy.size();j++)
			{ Task temp=taskscopy.get(j);
				not_deadlocked=true;
				for(int i=0;i<no_of_resource_types;i++)
				{
					if(temp.res_needed[i]>resource_units_copy[i]) //if the task can finish given the current resources
					{
						not_deadlocked=false;
						break;
					}
				}
				
				if(not_deadlocked)
				{
					safe=true; //simulate task finished and resources returned
					for(int i=0;i<no_of_resource_types;i++)
						resource_units_copy[i]+=temp.res_allocated[i]; 
					
					taskscopy.remove(temp);
					
				}
			}
				if(taskscopy.isEmpty())
					return true;
		}
			
		return false;
	}
	//function for granting/rejecting requests; works by checking safe state status
	public static void request_banker(Task task)
	{

			if(task.current_delay==0)
			{
				int res=task.current_act.res_type;
				int res_unit_requested=task.current_act.res_value;
				int res_unit_available=resource_units[res];
				int res_unit_needed=task.res_needed[res];
				int res_unit_allocated=task.res_allocated[res];
				
				boolean safe=false;
				//if request greater than claim, abort
				if(res_unit_requested>res_unit_needed)
					{
					task.state[1]=task.state[0]=true;
					blocked_tasks.add(task);
					}
				
			safe=safestates(task);
			task.res_needed[res]=res_unit_needed;
			task.res_allocated[res]=res_unit_allocated;
			//if the task is not aborted,we have resources and the state is safe, grant resources.
				if(!task.state[1])
				{
					if((res_unit_available>=res_unit_requested)&&(safe))
					{
						resource_units[res]=resource_units[res]-res_unit_requested;
						task.res_allocated[res]=task.res_allocated[res]+res_unit_requested;
						task.res_needed[res]=task.res_needed[res]-res_unit_requested;
						end(task);
					}
					
				else
					{
						task.wait++;
						blocked_tasks.add(task);
					}
				}
			}
			else
				if(task.current_delay>0)
					delayed(task);
	}
	//Runs fifo algorithm on the input
	public static void fifo_manager()
	{
		int cycle=0;
		Task current_task_fifo=null;
		dummy_tasks=new ArrayList<Task>();
		blocked_tasks=new ArrayList<Task>();
		running_tasks=new ArrayList<Task>();
		
		for(int i=0;i<no_of_tasks;i++)
			dummy_tasks.add(tasks[i]);
			
		while((dummy_tasks.size()>0))//keep running until all tasks have been executed
		{
			for(int i=0;i<dummy_tasks.size();i++)//take one activity of each task
			{
				current_task_fifo=dummy_tasks.get(i);
				String act_type1=current_task_fifo.current_act.act_type;
				if(act_type1.equals("initiate"))
					initiate(current_task_fifo,1);
				else
					if(act_type1.equals("request"))
						request_fifo(current_task_fifo);
					else
						if(act_type1.equals("release"))
							release(current_task_fifo);
						else	
							if(act_type1.equals("terminate"))
								terminate(current_task_fifo,cycle);
			}
			
			//checking for deadlocks; if all tasks are blocked then it is a deadlock
			if((blocked_tasks.size()>0) && (running_tasks.size()==0))
				deadlock();
				
			int i=0;
			while(i<no_of_resource_types)//adding all resources released back to the bank
			{
				resource_units[i]=resource_units[i]+accepted_resource_units[i];
				accepted_resource_units[i]=0;
				i++;
			}
			//clearing buffers and adding blocked tasks before running tasks
			dummy_tasks.clear();
			dummy_tasks.addAll(blocked_tasks);
			dummy_tasks.addAll(running_tasks);
			blocked_tasks.clear();
			running_tasks.clear();
			
		cycle++;
	}
	}
	//function to initiate a task.
	public static void initiate(Task task,int algo_type) //algo_type=1 for fifo, 0 for banker
	{
		if(task.current_delay==0)
		{task.res_needed[task.current_act.res_type]=task.current_act.res_value;
			if(algo_type==0)
			{
				//if claim is higher than resource units, abort task
				for(int i=0;i<no_of_resource_types;i++)
				{
					if(task.res_needed[i]>resource_units[i])
						task.state[1]=true;
				}
			}
			
			if(task.state[1]==false)
				end(task);//end()-> carries out next activity setting functionality towards the end
		}
		else
			if(task.current_delay>0)
				delayed(task);
	}
	//function to set the next activity of a task to be done in the next cycle; name means to be done in the end. not end the task
	public static void end(Task task)
	{
		int ind=task.activities.indexOf(task.current_act);
		task.current_act=task.activities.get(ind+1);
		task.current_delay=task.current_act.delay;
		running_tasks.add(task);
	}
	//function to make a task wait 
	public static void delayed(Task task)
	{
		task.state[2]=true;
		task.current_delay=task.current_delay-1;
		if(task.current_delay==0)
			task.state[2]=false;
		running_tasks.add(task);
	}
	//function to grant/reject requests for tasks using fifo manager
	public static void request_fifo(Task task) 
	{	
		if(task.current_delay==0)
		{
			int res=task.current_act.res_type;
			int res_unit_requested=task.current_act.res_value;
			int res_unit_available=resource_units[res];
			//grant if enough resources available in the bank else wait
			if(res_unit_available>=res_unit_requested)
			{	
				resource_units[res]=resource_units[res]-res_unit_requested;
				task.res_allocated[res]=task.res_allocated[res]+res_unit_requested;
				task.res_needed[res]=task.res_needed[res]-res_unit_requested;
				end(task);
			}
			else
			{	
				task.wait++;
				blocked_tasks.add(task);
			}
		}
		else
			if(task.current_delay>0)
				delayed(task);
		
	}
	//function to release resources
	public static void release(Task task) 
	{
		if(task.current_delay==0)
		{
			int res=task.current_act.res_type;
			int res_unit_released=task.current_act.res_value;
			int res_have_already=task.res_allocated[res];
			
			//cannot release if you dont have how many you want to release
			if(res_have_already>=res_unit_released)
			{	
				accepted_resource_units[res]+=res_unit_released;
				task.res_allocated[res]=task.res_allocated[res]-res_unit_released;
				task.res_needed[res]=task.res_needed[res]+res_unit_released;
				end(task);
			}
			else 
			{
				task.wait++;
				blocked_tasks.add(task);
			}
				
		}
		else
			if(task.current_delay>0)
				delayed(task);	
	}
	//function to terminate the task; sets state and finish time
	public static void terminate(Task task,int cycle) 
	{
		if(task.current_delay==0)
		{
			task.state[0]=true;
			task.total_time=cycle;
		}
		else
			if(task.current_delay>0)
				delayed(task);
		
	}
	//function to abort tasks in case of deadlocks; aborts all except the task with maximum id
	public static void deadlock()
	{
		int i=0,maxid=0,ind=0;
		
		for(int q=0;q<blocked_tasks.size();q++)
		{	
			if(maxid<blocked_tasks.get(q).id)
				{
					maxid=blocked_tasks.get(q).id;
					ind=q;
				}
		}
			
			Task t=blocked_tasks.get(ind);
			
				while(i<blocked_tasks.size()-1)
				{
					
					
					for(Task temp:blocked_tasks)
					{
						if(temp.id!=maxid)
						{	
							temp.state[0]=temp.state[1]=temp.state[3]=true;
							for(int j=0;j<no_of_resource_types;j++) //releasing held resources
							{
								accepted_resource_units[j]+=temp.res_allocated[j];
								temp.res_allocated[j]=0;
							}
							
						}
						
					}
					i++;
				}
			blocked_tasks.clear();
			blocked_tasks.add(t);//adding back the task with max id.
	}
	
	// Reads the input file and stores tasks/activities
	public static void read_file(String file_name) {
		
		File file = new File(file_name);
		
		int task_id=0;
		String act_type="";
		int delay=0;
		int res_type=0;
		int res_value=0;
		
		try {
			Scanner sc = new Scanner(file);
			no_of_tasks = sc.nextInt();
			no_of_resource_types= sc.nextInt();
			resource_units= new int[no_of_resource_types];
			accepted_resource_units=new int[no_of_resource_types];
			tasks=new Task[no_of_tasks];
			
			for(int i=0;i<no_of_resource_types;i++)
				{resource_units[i]=sc.nextInt();
				accepted_resource_units[i]=0;}
			for(int i=0;i<no_of_tasks;i++)
				tasks[i]=new Task(i+1,no_of_resource_types);
			while(sc.hasNext())
			{
				act_type=sc.next();
				task_id=sc.nextInt();
				delay=sc.nextInt();
				res_type=sc.nextInt();
				res_value=sc.nextInt();
				tasks[task_id-1].add_act(act_type,delay,res_type-1,res_value); //adds all activities related to a task to it.
			}
			
			for(int i=0;i<no_of_tasks;i++)
			{
				tasks[i].current_act=tasks[i].activities.get(0);
				tasks[i].current_delay=tasks[i].current_act.delay;
			}
			
			sc.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	  
}  


