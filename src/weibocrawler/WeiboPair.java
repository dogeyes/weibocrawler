package weibocrawler;

public class WeiboPair {

	private String first_id;
	
	private String second_id;

	public WeiboPair(String firstId, String secondId) {
		first_id = firstId;
		second_id = secondId;
	}

	public String getFirst_id() {
		return first_id;
	}

	public void setFirst_id(String firstId) {
		first_id = firstId;
	}

	public String getSecond_id() {
		return second_id;
	}

	public void setSecond_id(String secondId) {
		second_id = secondId;
	}
	
}
