
public class Process {

	private final Page[] pages;
	private final int duration;
	private final int arrivalTime;
	private int currentIndex;
	private int timeRemaining;
	private final String name;
	
	public Process(int arrival, String name) {
		int[] sizes = new int[] {5, 11, 17, 31};
		int size = sizes[(int)(Math.random() * 4)];
		pages = new Page[size];
		for(int i = 0; i < pages.length; i++) {
			pages[i] = new Page("Page " + i, this);
		}
		duration = ((int)(Math.random() * 5) + 1) * 10;
		arrivalTime = arrival;
		currentIndex = 0;
		timeRemaining = duration;
		this.name = name;
	}
	
	public int getSize() {
		return pages.length;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getArrivalTime() {
		return arrivalTime;
	}
	
	public int getRemainingTime() {
		return timeRemaining;
	}
	
	public void reduceTimeRemaining(int amount) {
		timeRemaining -= amount;
	}
	
	public Page getFirstPage() {
		return pages[0];
	}
	
	public Page getNextPage() {
		int r = (int) (Math.random() * 10);
		int j = currentIndex;
		if(r < 7) {
			j = j + ((int)(Math.random() * 3) - 1);
		}else {
			if(Math.random() < 0.5) {
				j = (int)(Math.random() * (currentIndex-1));
			}else {
				j = (int)(Math.random() * (getSize() - (currentIndex + 2))) + currentIndex + 2;
			}
		}
		if(j < 0) {
			j = 0;
		}
		if(j >= getSize()) {
			j = getSize() - 1;
		}
		currentIndex = j;
		//System.out.println(j);
		return pages[j];
	}
	
	public Page[] getPages() {
		return pages;
	}

	public String toString() {
		return name;
	}
}
