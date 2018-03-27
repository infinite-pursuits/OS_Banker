public class Activity {
	
	String act_type;
	int delay;
	int res_type;
	int res_value;
	
	//Empty constructor
	public Activity() {
		act_type = "";
		delay = 0;
		res_type = 0;
		res_value = 0;
	}
	
	// Constructor to set variables to given values
	public Activity(String act_type, int delay, int res_type, int res_value) {
		this.act_type = act_type;
		this.delay = delay;
		this.res_type = res_type;
		this.res_value = res_value;
	}
	
}
