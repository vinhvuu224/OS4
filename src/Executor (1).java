import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Executor {
	private static String[] memoryMap = new String[100];
	public static LinkedList<Process> generateJobQueue() {
		LinkedList<Process> ret = new LinkedList<Process>();
		for (int i = 0; i < 150; i++) {
			ret.add(new Process((int) (Math.random() * 600), "P" + i));
		}
		ret.sort((p1, p2) -> (int) Math.signum(p1.getArrivalTime() - p2.getArrivalTime()));
		return ret;
	}

	public static void main(String[] args) {
		int repetitions = 5;
		
		double[] fifo = new double[3];
		System.out.println("FIFO:");
		for (int i = 0; i < repetitions; i++) {
			int[] temp = FIFO(false, false, false);
			fifo[0] = fifo[0] + temp[0];
			fifo[1] = fifo[1] + temp[1];
			fifo[2] = fifo[2] + temp[2];
		}
		fifo[0] = fifo[0] / repetitions;
		fifo[1] = fifo[1] / repetitions;
		fifo[2] = fifo[2] / repetitions;
		System.out.println(fifo[0] + " hits\n" + fifo[1] + " misses\n" + fifo[2] + " processes started\n");

		double[] lru = new double[3];
		System.out.println("LRU:");
		for (int i = 0; i < repetitions; i++) {
			int[] temp = LRU(false, false, false);
			lru[0] = lru[0] + temp[0];
			lru[1] = lru[1] + temp[1];
			lru[2] = lru[2] + temp[2];
		}
		lru[0] = lru[0] / repetitions;
		lru[1] = lru[1] / repetitions;
		lru[2] = lru[2] / repetitions;
		System.out.println(lru[0] + " hits\n" + lru[1] + " misses\n" + lru[2] + " processes started\n");

		double[] lfu = new double[3];
		System.out.println("LFU:");
		for (int i = 0; i < repetitions; i++) {
			int[] temp = LFU(false, false, false);
			lfu[0] = lfu[0] + temp[0];
			lfu[1] = lfu[1] + temp[1];
			lfu[2] = lfu[2] + temp[2];
		}
		lfu[0] = lfu[0] / repetitions;
		lfu[1] = lfu[1] / repetitions;
		lfu[2] = lfu[2] / repetitions;
		System.out.println(lfu[0] + " hits\n" + lfu[1] + " misses\n" + lfu[2] + " processes started\n");

		double[] mfu = new double[3];
		System.out.println("MFU:");
		for (int i = 0; i < repetitions; i++) {
			int[] temp = MFU(false, false, false);
			mfu[0] = mfu[0] + temp[0];
			mfu[1] = mfu[1] + temp[1];
			mfu[2] = mfu[2] + temp[2];
		}
		mfu[0] = mfu[0] / repetitions;
		mfu[1] = mfu[1] / repetitions;
		mfu[2] = mfu[2] / repetitions;
		System.out.println(mfu[0] + " hits\n" + mfu[1] + " misses\n" + mfu[2] + " processes started\n");

		double[] rand = new double[3];
		System.out.println("Rand:");
		for (int i = 0; i < repetitions; i++) {
			int[] temp = randomPick(false, false, false);
			rand[0] = rand[0] + temp[0];
			rand[1] = rand[1] + temp[1];
			rand[2] = rand[2] + temp[2];
		}
		rand[0] = rand[0] / repetitions;
		rand[1] = rand[1] / repetitions;
		rand[2] = rand[2] / repetitions;
		System.out.println(rand[0] + " hits\n" + rand[1] + " misses\n" + rand[2] + " processes started\n");
	}

	public static Page setPage(Page[] memory, Page page, int index, int time, Process caller, boolean verbose) {
		Page replaced = memory[index];
		boolean replacing = replaced != null;
		if (replacing) {
			replaced.setIndex(-1);
		}
		memory[index] = page;
		page.setIndex(index);
		page.setTime(time);
		page.access(time);
		if (verbose)
			System.out.println("Time: " + (time / 10.0) + "seconds | Process: " + caller + " | Page referenced: " + page
					+ " | Memory Page: " + index + " | Process/Page Replaced: "
					+ (replaced == null ? "" : replaced.getOwner() + " ") + replaced + " | miss");
		return replaced;
	}

	/**
	 * Uses FIFO to determine which page to replace
	 */
	public static int[] FIFO(boolean byReferenceCount, boolean verbose, boolean verbose2) {
		int currentIndex=0;
		ArrayList<Process> currentJobs = new ArrayList<Process>();
		LinkedList<Process> allJobs = generateJobQueue();
		
		for(int i = 0;i<memoryMap.length;i++) {
			memoryMap[i]=".";
		}
		Page[] memoryPages = new Page[100];
		int freePages = 100, hitCount = 0, missCount = 0, referenceCount = 0, processesStarted = 0;
		for (int i = 0; (i < 600 && !byReferenceCount) || (byReferenceCount && referenceCount < 100); i++) {
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : currentJobs) {
				Page page = p.getNextPage();
				if (page.getIndex() == -1) {
					Page replaced = setPage(memoryPages, page, FIFOChoose(memoryPages), i, p, verbose);
					if (replaced == null) {
						freePages--;
						memoryMap[currentIndex] = p.toString();
						for(int j = 0;j<memoryMap.length;j++) {
							if(memoryMap[j]==".") {
								currentIndex=j;
								break;
							}
						}
					}
					missCount++;
				} else {
					page.access(i);
					if (verbose)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Page referenced: "
								+ page + " | Memory Page: " + page.getIndex() + " | Process/Page Replaced: null | hit");
					hitCount++;
				}
				p.reduceTimeRemaining(1);
				if (p.getRemainingTime() <= 0) {
					Page[] pages = p.getPages();
					for (Page pageToRemove : pages) {
						if (pageToRemove.getIndex() != -1) {
							memoryPages[pageToRemove.getIndex()] = null;
							pageToRemove.setIndex(-1);
							memoryMap[currentIndex]=".";
							for(int j = 0;j<memoryMap.length;j++) {
								if(memoryMap[j]==".") {
									currentIndex=j;
									break;
								}
							}
							freePages++;
						}
					}
					if (verbose2)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Exit | Size: "
								+ p.getSize() + " | Service Duration " + p.getDuration()+" | Map: "+Arrays.toString(memoryMap));
					toRemove.add(p);
					for(Process proc: toRemove) {
						for (int j = 0;j<memoryMap.length;j++) {
							if (memoryMap[j]==proc.toString()) {
								memoryMap[j]=".";
							}
						}
					}
				}
				referenceCount++;
			}
			
			
			currentJobs.removeAll(toRemove);
			while (allJobs.size() > 0 && allJobs.peek().getArrivalTime() <= i && freePages >= 4) {
				Process p = allJobs.pop();
				currentJobs.add(p);
				Page replaced = setPage(memoryPages, p.getFirstPage(), FIFOChoose(memoryPages), i, p, verbose);
				if (replaced == null) {
					memoryMap[currentIndex] = p.toString();
					for(int j = 0;j<memoryMap.length;j++) {
						if(memoryMap[j]==".") {
							currentIndex=j;
							break;
						}
					}
					freePages--;
				}
				if (verbose2)
					System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Enter | Size: "
							+ p.getSize() + " | Service Duration " + (p.getDuration() / 10.0)+" | Map: "+Arrays.toString(memoryMap));
				referenceCount++;
				processesStarted++;
			}
		}
		return new int[] { hitCount, missCount, processesStarted };
	}

	private static int FIFOChoose(Page[] pages) {
		int index = 0;
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				return i;
			}
			if (pages[index].getTimeSet() > pages[i].getTimeSet()) {
				index = i;
			}
		}
		return index;
	}

	// ---

	/**
	 * Uses LRU to determine which page to replace
	 */
	public static int[] LRU(boolean byReferenceCount, boolean verbose, boolean verbose2) {
		int currentIndex=0;
		ArrayList<Process> currentJobs = new ArrayList<Process>();
		LinkedList<Process> allJobs = generateJobQueue();
		
		for(int i = 0;i<memoryMap.length;i++) {
			memoryMap[i]=".";
		}
		
		Page[] memoryPages = new Page[100];
		int freePages = 100, hitCount = 0, missCount = 0, referenceCount = 0, processesStarted = 0;
		for (int i = 0; (i < 600 && !byReferenceCount) || (byReferenceCount && referenceCount < 100); i++) {
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : currentJobs) {
				Page page = p.getNextPage();
				if (page.getIndex() == -1) {
					Page replaced = setPage(memoryPages, page, LRUChoose(memoryPages), i, p, verbose);
					if (replaced == null) {
						memoryMap[currentIndex] = p.toString();
						for(int j = 0;j<memoryMap.length;j++) {
							if(memoryMap[j]==".") {
								currentIndex=j;
								break;
							}
						}
						freePages--;
						
					}
					missCount++;
				} else {
					page.access(i);
					if (verbose)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Page referenced: "
								+ page + " | Memory Page: " + page.getIndex() + " | Process/Page Replaced: null | hit");
					hitCount++;
				}
				p.reduceTimeRemaining(1);
				if (p.getRemainingTime() <= 0) {
					Page[] pages = p.getPages();
					for (Page pageToRemove : pages) {
						if (pageToRemove.getIndex() != -1) {
							memoryPages[pageToRemove.getIndex()] = null;
							pageToRemove.setIndex(-1);
							memoryMap[currentIndex]=".";
							for(int j = 0;j<memoryMap.length;j++) {
								if(memoryMap[j]==".") {
									currentIndex=j;
									break;
								}
							}
							freePages++;
						}
					}
					if (verbose2)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Exit | Size: "
								+ p.getSize() + " | Service Duration " + p.getDuration()+" | Map: "+Arrays.toString(memoryMap));
					toRemove.add(p);
					for(Process proc: toRemove) {
						for (int j = 0;j<memoryMap.length;j++) {
							if (memoryMap[j]==proc.toString()) {
								memoryMap[j]=".";
							}
						}
					}
				}
				referenceCount++;
			}
			currentJobs.removeAll(toRemove);
			while (allJobs.size() > 0 && allJobs.peek().getArrivalTime() <= i && freePages >= 4) {
				Process p = allJobs.pop();
				currentJobs.add(p);
				Page replaced = setPage(memoryPages, p.getFirstPage(), LRUChoose(memoryPages), i, p, verbose);
				if (replaced == null) {
					for(int j = 0;j<memoryMap.length;j++) {
						if(memoryMap[j]==".") {
							currentIndex=j;
							break;
						}
					}
					freePages--;
				}
				if (verbose2)
					System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Enter | Size: "
							+ p.getSize() + " | Service Duration " + (p.getDuration() / 10.0)+" | Map: "+Arrays.toString(memoryMap));
				referenceCount++;
				processesStarted++;
			}
		}
		return new int[] { hitCount, missCount, processesStarted };
	}

	public static int LRUChoose(Page[] pages) {
		int index = 0;
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				return i;
			}
			if (pages[index].getTimeUsed() > pages[i].getTimeUsed()) {
				index = i;
			}
		}
		return index;
	}

	// ---

	/**
	 * Uses LFU to determine which page to replace
	 */
	public static int[] LFU(boolean byReferenceCount, boolean verbose, boolean verbose2) {
		int currentIndex=0;
		ArrayList<Process> currentJobs = new ArrayList<Process>();
		LinkedList<Process> allJobs = generateJobQueue();
		
		for(int i = 0;i<memoryMap.length;i++) {
			memoryMap[i]=".";
		}
		
		Page[] memoryPages = new Page[100];
		int freePages = 100, hitCount = 0, missCount = 0, referenceCount = 0, processesStarted = 0;
		for (int i = 0; (i < 600 && !byReferenceCount) || (byReferenceCount && referenceCount < 100); i++) {
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : currentJobs) {
				Page page = p.getNextPage();
				if (page.getIndex() == -1) {
					Page replaced = setPage(memoryPages, page, LFUChoose(memoryPages), i, p, verbose);
					if (replaced == null) {
						memoryMap[currentIndex] = p.toString();
						for(int j = 0;j<memoryMap.length;j++) {
							if(memoryMap[j]==".") {
								currentIndex=j;
								break;
							}
						}
						freePages--;
					}
					missCount++;
				} else {
					page.access(i);
					if (verbose)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Page referenced: "
								+ page + " | Memory Page: " + page.getIndex() + " | Process/Page Replaced: null | hit");
					hitCount++;
				}
				p.reduceTimeRemaining(1);
				if (p.getRemainingTime() <= 0) {
					Page[] pages = p.getPages();
					for (Page pageToRemove : pages) {
						if (pageToRemove.getIndex() != -1) {
							memoryPages[pageToRemove.getIndex()] = null;
							pageToRemove.setIndex(-1);
							
							memoryMap[currentIndex]=".";
							for(int j = 0;j<memoryMap.length;j++) {
								if(memoryMap[j]==".") {
									currentIndex=j;
									break;
								}
							}
							
							freePages++;
						}
					}
					if (verbose2)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Exit | Size: "
								+ p.getSize() + " | Service Duration " + p.getDuration()+" | Map: "+Arrays.toString(memoryMap));
					toRemove.add(p);
					for(Process proc: toRemove) {
						for (int j = 0;j<memoryMap.length;j++) {
							if (memoryMap[j]==proc.toString()) {
								memoryMap[j]=".";
							}
						}
					}
				}
				referenceCount++;
			}
			currentJobs.removeAll(toRemove);
			while (allJobs.size() > 0 && allJobs.peek().getArrivalTime() <= i && freePages >= 4) {
				Process p = allJobs.pop();
				currentJobs.add(p);
				Page replaced = setPage(memoryPages, p.getFirstPage(), LFUChoose(memoryPages), i, p, verbose);
				if (replaced == null) {
					for(int j = 0;j<memoryMap.length;j++) {
						if(memoryMap[j]==".") {
							currentIndex=j;
							break;
						}
					}
					freePages--;
				}
				if (verbose2)
					System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Enter | Size: "
							+ p.getSize() + " | Service Duration " + (p.getDuration() / 10.0)+" | Map: "+Arrays.toString(memoryMap));
				referenceCount++;
				processesStarted++;
			}
		}
		return new int[] { hitCount, missCount, processesStarted };
	}

	public static int LFUChoose(Page[] pages) {
		int index = 0;
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				return i;
			}
			if (pages[index].getTimeUsed() > pages[i].getTimeUsed()) {
				index = i;
			}
		}
		return index;
	}

	// ---

	/**
	 * Uses MFU to determine which page to replace
	 */
	public static int[] MFU(boolean byReferenceCount, boolean verbose, boolean verbose2) {
		int currentIndex=0;
		ArrayList<Process> currentJobs = new ArrayList<Process>();
		LinkedList<Process> allJobs = generateJobQueue();
		
		for(int i = 0;i<memoryMap.length;i++) {
			memoryMap[i]=".";
		}
		
		Page[] memoryPages = new Page[100];
		int freePages = 100, hitCount = 0, missCount = 0, referenceCount = 0, processesStarted = 0;
		for (int i = 0; (i < 600 && !byReferenceCount) || (byReferenceCount && referenceCount < 100); i++) {
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : currentJobs) {
				Page page = p.getNextPage();
				if (page.getIndex() == -1) {
					Page replaced = setPage(memoryPages, page, MFUChoose(memoryPages), i, p, verbose);
					if (replaced == null) {
						memoryMap[currentIndex] = p.toString();
						for(int j = 0;j<memoryMap.length;j++) {
							if(memoryMap[j]==".") {
								currentIndex=j;
								break;
							}
						}
						freePages--;
					}
					missCount++;
				} else {
					page.access(i);
					if (verbose)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Page referenced: "
								+ page + " | Memory Page: " + page.getIndex() + " | Process/Page Replaced: null | hit");
					hitCount++;
				}
				p.reduceTimeRemaining(1);
				if (p.getRemainingTime() <= 0) {
					Page[] pages = p.getPages();
					for (Page pageToRemove : pages) {
						if (pageToRemove.getIndex() != -1) {
							memoryPages[pageToRemove.getIndex()] = null;
							pageToRemove.setIndex(-1);
							memoryMap[currentIndex]=".";
							for(int j = 0;j<memoryMap.length;j++) {
								if(memoryMap[j]==".") {
									currentIndex=j;
									break;
								}
							}
							freePages++;
						}
					}
					if (verbose2)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Exit | Size: "
								+ p.getSize() + " | Service Duration " + p.getDuration()+" | Map: "+Arrays.toString(memoryMap));
					toRemove.add(p);
					for(Process proc: toRemove) {
						for (int j = 0;j<memoryMap.length;j++) {
							if (memoryMap[j]==proc.toString()) {
								memoryMap[j]=".";
							}
						}
					}
				}
				referenceCount++;
			}
			currentJobs.removeAll(toRemove);
			while (allJobs.size() > 0 && allJobs.peek().getArrivalTime() <= i && freePages >= 4) {
				Process p = allJobs.pop();
				currentJobs.add(p);
				Page replaced = setPage(memoryPages, p.getFirstPage(), MFUChoose(memoryPages), i, p, verbose);
				if (replaced == null) {
					for(int j = 0;j<memoryMap.length;j++) {
						if(memoryMap[j]==".") {
							currentIndex=j;
							break;
						}
					}
					freePages--;
				}
				if (verbose2)
					System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Enter | Size: "
							+ p.getSize() + " | Service Duration " + (p.getDuration() / 10.0)+" | Map: "+Arrays.toString(memoryMap));
				referenceCount++;
				processesStarted++;
			}
		}
		return new int[] { hitCount, missCount, processesStarted };
	}

	public static int MFUChoose(Page[] pages) {
		int index = 0;
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				return i;
			}
			if (pages[index].getTimeUsed() < pages[i].getTimeUsed()) {
				index = i;
			}
		}
		return index;
	}

	// ---

	/**
	 * Randomly determine which page to replace
	 */
	public static int[] randomPick(boolean byReferenceCount, boolean verbose, boolean verbose2) {
		int currentIndex=0;
		ArrayList<Process> currentJobs = new ArrayList<Process>();
		LinkedList<Process> allJobs = generateJobQueue();
		
		for(int i = 0;i<memoryMap.length;i++) {
			memoryMap[i]=".";
		}
		
		Page[] memoryPages = new Page[100];
		int freePages = 100, hitCount = 0, missCount = 0, referenceCount = 0, processesStarted = 0;
		for (int i = 0; (i < 600 && !byReferenceCount) || (byReferenceCount && referenceCount < 100); i++) {
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : currentJobs) {
				Page page = p.getNextPage();
				if (page.getIndex() == -1) {
					Page replaced = setPage(memoryPages, page, randomChoose(memoryPages), i, p, verbose);
					if (replaced == null) {
						memoryMap[currentIndex] = p.toString();
						for(int j = 0;j<memoryMap.length;j++) {
							if(memoryMap[j]==".") {
								currentIndex=j;
								break;
							}
						}
						freePages--;
					}
					missCount++;
				} else {
					page.access(i);
					if (verbose)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Page referenced: "
								+ page + " | Memory Page: " + page.getIndex() + " | Process/Page Replaced: null | hit");
					hitCount++;
				}
				p.reduceTimeRemaining(1);
				if (p.getRemainingTime() <= 0) {
					Page[] pages = p.getPages();
					for (Page pageToRemove : pages) {
						if (pageToRemove.getIndex() != -1) {
							memoryPages[pageToRemove.getIndex()] = null;
							pageToRemove.setIndex(-1);
							memoryMap[currentIndex]=".";
							for(int j = 0;j<memoryMap.length;j++) {
								if(memoryMap[j]==".") {
									currentIndex=j;
									break;
								}
							}
							freePages++;
						}
					}
					if (verbose2)
						System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Exit | Size: "
								+ p.getSize() + " | Service Duration " + p.getDuration()+" | Map: "+Arrays.toString(memoryMap));
					toRemove.add(p);
					for(Process proc: toRemove) {
						for (int j = 0;j<memoryMap.length;j++) {
							if (memoryMap[j]==proc.toString()) {
								memoryMap[j]=".";
							}
						}
					}
				}
				referenceCount++;
			}
			currentJobs.removeAll(toRemove);
			while (allJobs.size() > 0 && allJobs.peek().getArrivalTime() <= i && freePages >= 4) {
				Process p = allJobs.pop();
				currentJobs.add(p);
				Page replaced = setPage(memoryPages, p.getFirstPage(), randomChoose(memoryPages), i, p, verbose);
				if (replaced == null) {
					for(int j = 0;j<memoryMap.length;j++) {
						if(memoryMap[j]==".") {
							currentIndex=j;
							break;
						}
					}
					freePages--;
				}
				if (verbose2)
					System.out.println("Time: " + (i / 10.0) + "seconds | Process: " + p + " | Enter | Size: "
							+ p.getSize() + " | Service Duration " + (p.getDuration() / 10.0)+" | Map: "+Arrays.toString(memoryMap));
				referenceCount++;
				processesStarted++;
			}
		}
		return new int[] { hitCount, missCount, processesStarted };
	}

	public static int randomChoose(Page[] pages) {
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] == null) {
				return i;
			}
		}
		return (int) (Math.random() * pages.length);
	}
}