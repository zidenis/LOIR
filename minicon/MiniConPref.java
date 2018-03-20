/*
 *   Copyright 2015 Cheikh BA <cheikh.ba.sn@gmail.com>
 *
 *   This file is part of LOIR.
 *
 *   LOIR is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LOIR is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License,
 *   along with LOIR.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
 * Created on 09.2015 - Cheikh Ba
 * 
 * Based On Programming project - Implementation of MiniCon algorithm (Created on 20.01.2005)
 * 
 */
package minicon;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import preference.Organiser;
import preference.PreferencesFileParser;
import datalog.DatalogQuery;

/**
 * 
 * MiniCon is the main class of the implementation of the MiniCon algorithm. It
 * contains the main method to start the program. It uses class InputHandler to
 * obtain parsed user input in form of a MiniCon object. It contains the query
 * and a list of views. Basically, the algorithm consists of three steps: 1.
 * forming the MCDs, 2. combining the MCD, and 3. removing redundant subgoals
 * The last part is optional.
 * 
 * @author Kevin Irmscher
 */
public class MiniConPref {
	
	private static int testID;	
	private static long numberOfRequiredRewritings;

	/** query Object used by algorithm */
	private DatalogQuery query;

	/** list of views used by algorithm */
	private List<DatalogQuery> views;

	/** list of rewritings created by the algorithm */ 
	private List<Rewriting> rewritings;

	/**
	 * MiniCon constructor
	 * 
	 * @param query
	 *            query obtained from the parser
	 * @param views
	 *            list of views obtained from the parser
	 */
	public MiniConPref(DatalogQuery query, List<DatalogQuery> views) {
		this.query = query;
		this.views = views;
		this.rewritings = new ArrayList<Rewriting>();
	}

	/**
	 * Main method will be called to start the algorithm. It uses class
	 * InputHandler to handle the arguments provided by parameter args.
	 * InputHandler will return a MiniCon object which contains the query and a
	 * list of views.
	 * 
	 * @param args
	 *            -v : verbose mode (print MCDs);
	 * 
	 * -f FILE.XML ID : read testcase with ID from file;
	 * 
	 * -sql : SQL input mode;
	 * 
	 * -r : remove redundancies
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("MiniCon WALO Algorithm");
		
		//testID = 5;
		//numberOfRequiredRewritings = 4;
		
		//**/ args = new String[]{"1"};
		
		if (args.length < 1 || args.length > 2){
			System.out.println("USAGE: java minicon.MiniConPref testId [numberOfRequiredRewritings]");
			System.exit(1);
		}
		if (args.length == 1){
			testID = Integer.valueOf(args[0]).intValue();
			numberOfRequiredRewritings = 999999999999999999L; // all rewritings are desired !
		} else {
			testID = Integer.valueOf(args[0]).intValue();
			numberOfRequiredRewritings = Long.valueOf(args[1]).longValue();
		}
		
		long start = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
					
		MiniConPref mc = InputHandlerPref.handleArguments(new String[]{"-f", "testcases.xml", "" + testID});		
		
		if (mc != null) {
			mc.printQuery(); 			// commented for time evaluation
			mc.printViews();			// commented for time evaluation			
			mc.startMiniCon();
			mc.printRewritings();		// commented for time evaluation
			
		}	
		long time = ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() - start;
		System.out.println("Done in: " + time/1000000.0 + " ms");
	}

	
	/**
	 * The method will execute the actual algorithm. Three method calls will be
	 * performed regarding to the three parts of the algorithm. 1. forming MCDs,
	 * 2. combining MCDs, 3. remove redundancies; the last call depends on
	 * whether argument -r is provided
	 */
	public void startMiniCon() {
		
		/*C.BA*/ 
		// set the MCD preferences ...
		try {
			
			PreferencesFileParser.setViewsPreferences (views, "preferences.xml", testID); 
			Organiser.createOrganiser(views, query); 
			
			rewritings = Organiser.getDesiredNumberOfRewritings(query, numberOfRequiredRewritings);	
				
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("OUPSS !!! " + e);
			//System.out.println("mcds.size() = " + mcds.size());
			System.out.println("rewritings.size() = " + rewritings.size());
		}				
			
	}

	/**
	 * Print rewritings
	 */
	private void printRewritings() {
		if (!rewritings.isEmpty()) {
			System.out.println("\nRewriting(s):");
			for (Rewriting rw : rewritings) {
				System.out.println(rw);
			}
		}
	}
	
	/**
	 * Print query provided by user
	 */
	private void printQuery() {
		System.out.println("\nQuery: " + query);
	}

	/**
	 * Print views provided by user
	 */
	private void printViews() {
		for (DatalogQuery view : views) {
			System.out.println("View: " + view);

		}
	}

	/**
	 * Returns list of Rewriting objects created by the algorithm.
	 * 
	 * @return list of Rewriting objects
	 */
	public List<Rewriting> getRewritings() {
		return rewritings;
	}

}
