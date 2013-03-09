package data;

import java.util.ArrayList;

/**
 * @author Tianyi Wang
 */
public class DataInstance {
	private int label;					// either +1 or -1.
	private ArrayList<Integer> attributes;

	public DataInstance(String line) {
		attributes = new ArrayList<Integer>();
		
		String[] all = line.split("\t");
		if (all[0].charAt(0) == '+') {
			all[0] = all[0].substring(1);
		}
		label = Integer.parseInt(all[0]);
		
		for (int i = 1; i < all.length; i++) {
			if (all[i].charAt(0) == '+') {
				all[i] = all[i].substring(1);
			}
			attributes.add(Integer.parseInt(all[i]));
		}
	}
	
	public int getLabel() {
		return label;
	}
	
	public ArrayList<Integer> getAttributes() {
		return attributes;
	}

	/**
	 * For debugging purpose.
	 */
	@Override
	public String toString() {
		return "DataInstance [label=" + label + ", attributes=" + attributes
				+ "]";
	}
}