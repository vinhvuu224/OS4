
public class Page {

	private int timeSet = 0;
	private int timeUsed = 0;
	private int index = -1;
	private int accessCount = 0;
	private final String name;
	private final Process owner;
	
	public Page(String name, Process owner) {
		this.name = name;
		this.owner = owner;
	}
	
	public int getTimeSet() {
		return timeSet;
	}
	
	public void setTime(int time) {
		timeSet = time;
	}
	
	public int getTimeUsed() {
		return timeUsed;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getAccessCount() {
		return accessCount;
	}
	
	public void access(int time) {
		timeUsed = time;
		accessCount++;
	}
	
	public Process getOwner() {
		return owner;
	}
	
	public String toString() {
		return name;
	}
}
