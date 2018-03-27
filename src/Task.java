import java.util.*;

public class Task {
	
	int id=-1;
	Activity current_act=null;
	ArrayList<Activity> activities=null;
	int[] res_allocated=null;
	int[] res_needed=null;
	int current_delay=0;
	int wait=0;
	int total_time=0;
	boolean[] state=new boolean[4];//terminate,aborted,delayed,complete
	
	//constructor to initialize task
	public Task(int id,int no_of_resource_types)
	{
		this.id=id;
		this.current_act = new Activity();
		this.activities = new ArrayList<Activity>();
		this.res_allocated = new int[no_of_resource_types];
		this.res_needed = new int[no_of_resource_types];
		for (int i = 0; i < no_of_resource_types; i++) {
			res_allocated[i] = 0;
			res_needed[i] = 0;
			
		}
		  Arrays.fill(this.state, false);
		
	}
	//for adding acitivities related to a particular task to the task
	public void add_act(String act_type,int delay,int res_type,int res_value)
	{	Activity act=new Activity(act_type,delay,res_type,res_value);
		this.activities.add(act);
	}
	
	//for displaying the wait and total time and wait percentage for each task
	public void display()
	{
		float waitper = (float)wait /total_time * 100;
		
		if (!this.state[1]) {
			System.out.print("Task" + id);
			System.out.printf("	%d	%d	%.0f%%",total_time,wait,waitper);
			System.out.println("");
		} else {
			System.out.print("Task" + id);
			System.out.print("	Aborted");
			System.out.println("");
		}
	}
	
}
