
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
 * Created on 14.05.2014
 * Modified on sept 2015
 * Organiser: to store views (concrete services) according to the user's preferences.
 * 
 * @author Cheikh BA
 */

package preference;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import datalog.DatalogQuery;
import datalog.Predicate;
import datalog.PredicateElement;
import minicon.MCD;
import minicon.MCDMappings;
import minicon.Mapping;
import minicon.Rewriting;

public class Organiser {
	
private static Hashtable<Predicate, LinkedHashMap <String, List<DatalogQuery>>> organiser;
	
	public static void createOrganiser (List<DatalogQuery> views, DatalogQuery query){
		LinkedHashMap <String, List<DatalogQuery>> abstractService; 
		List<DatalogQuery> coverageDomain;
		
		organiser = new  Hashtable<Predicate, LinkedHashMap <String, List<DatalogQuery>>>();		
		
		for (int i = 0; i < query.getPredicates().size(); i++){ 
			Predicate subGoal = query.getPredicates().get(i); 			
			coverageDomain = getCoverageDomain(subGoal, views); 		
			abstractService = new LinkedHashMap<String, List<DatalogQuery>>(); 
			abstractService.putAll(rankViews(coverageDomain)); 
			organiser.put(subGoal, abstractService);
		}				
	}
	
	public static Hashtable<Predicate, LinkedHashMap <String, List<DatalogQuery>>> getOrganiser (){
		return organiser;
	}	
		 
	public static List<Rewriting> getDesiredNumberOfRewritings (DatalogQuery query, long desiredNumberOfRewritings){ 
		List<Rewriting> rewritings = new ArrayList<Rewriting>();
				
		try {
			setDesiredNumberOfRewriting(rewritings, new ArrayList<MCD>(), query.getPredicates(), query, desiredNumberOfRewritings);
		} catch (Exception e) {
			// The desired number of rewriting is reached
		}
		
		return rewritings;	
	}
	
	private static void setDesiredNumberOfRewriting(List<Rewriting> rewritings, List<MCD> rewritingPrefix, List<Predicate> subGoals, DatalogQuery query, long desiredNumberOfRewritings) throws Exception{
		
		if (rewritings.size() >= desiredNumberOfRewritings)
			throw new Exception("The desired number of rewriting is reached ...");
		
		if (subGoals.size() == 0){
			if (isRewriting(rewritingPrefix, query))
				rewritings.add(new Rewriting(rewritingPrefix, query));
		} else {
			List<MCD> sortedCoverageDomain = getMCDs(getSortedCoverageDomain(subGoals.get(0)), /*subGoals*/ subGoals.get(0), query);
			
			// REMOVE DUPLICATE ???			
			//sortedCoverageDomain = removeDuplicates(sortedCoverageDomain);
									
			for (int i = 0 ; i < sortedCoverageDomain.size(); i++){
				MCD mcd = sortedCoverageDomain.get(i);
				List<MCD> newRewritingPrefix = new ArrayList<MCD>(rewritingPrefix);
				newRewritingPrefix.add(mcd);
				List<Predicate> remainingPredicatesToCover = getRemainingPredicatesToCover(mcd, subGoals);
				setDesiredNumberOfRewriting(rewritings, newRewritingPrefix, remainingPredicatesToCover, query, desiredNumberOfRewritings);
			}
		}
	}	
	
	private static List<Predicate> getRemainingPredicatesToCover(MCD mcd, List<Predicate> subGoals){
		List<Predicate> remainingPredicatesToCover = new ArrayList<Predicate>();
		for (int i = 0; i < subGoals.size(); i++){
			if (! mcd.getSubgoals().contains(subGoals.get(i)))
				remainingPredicatesToCover.add(subGoals.get(i));
		}
		
		return remainingPredicatesToCover;
	}
	
	private static List<DatalogQuery> getSortedCoverageDomain (Predicate abstractService){
		//if (abstractService.isCoverageDomainProcessed()) // If it's already computed !
			//return abstractService.getSortedCoverageDomain();
		if (abstractService.isCoverageDomainProcessed()) // If it's already computed !
			return abstractService.getCoverageDomain();
		
		// else : the coverage domain for this abstract service is not yet processed !
		LinkedHashMap<String, List<DatalogQuery>> rankedViews = organiser.get(abstractService);
		List<DatalogQuery> result = new ArrayList<DatalogQuery>();
		String rank;
		Iterator <String> iterator = rankedViews.keySet().iterator();

		while (iterator.hasNext()){
					rank = iterator.next();
					result.addAll(rankedViews.get(rank));
				}	
		//if (abstractService.getSortedCoverageDomain() == null) // always the case
			//abstractService.setSortedCoverageDomain(result);
		abstractService.setCoverageDomain(result);
		
		return result;
	}
		
	private static List<DatalogQuery> getCoverageDomain(Predicate abstractService, List<DatalogQuery> views){
		List<DatalogQuery> coverageDomain = new LinkedList<DatalogQuery>();
		
		for (int i = 0; i < views.size(); i++){
			DatalogQuery view = views.get(i);
			List<Predicate> coveredSubGoals = view.getPredicates();
			for (int j = 0; j < coveredSubGoals.size(); j++ ){
				if (coveredSubGoals.get(j).name.equals(abstractService.name)){
					coverageDomain.add(view);
					j = coveredSubGoals.size();
				}
			}			
		}

		return coverageDomain;
	}
	
	private static LinkedHashMap <String, List<DatalogQuery>> rankViews (List<DatalogQuery> views){
		LinkedHashMap <String, List<DatalogQuery>> rank = new LinkedHashMap <String, List<DatalogQuery>>();
		
		if (views.size() > 0){
			
			List<DatalogQuery> viewList = new LinkedList<DatalogQuery>();
			viewList.add(0, views.get(0)); 
			rank.put("" + views.get(0).getRank(), viewList);
			
			for (int i = 1 ; i < views.size(); i++){
				
				if (rank.keySet().contains(views.get(i).getRank()+"")){ // le rank existe d�j�
					rank.get(views.get(i).getRank()+"").add(views.get(i));
				} else {
					viewList = new LinkedList<DatalogQuery>();
					viewList.add(0, views.get(i));
					rank.put("" + views.get(i).getRank(), viewList);
				}
			}
			
		}
		
		sortRankedViews(rank); // sort, for each subgoal, the ranks
		
		return rank;
	}
	
	private static void sortRankedViews (LinkedHashMap <String, List<DatalogQuery>> ranks){
		LinkedHashMap <String, List<DatalogQuery>> temp = new LinkedHashMap<String, List<DatalogQuery>> ();		
		LinkedList<String> rankValues = new LinkedList<String>();
		for (String rank: ranks.keySet()){
			rankValues.add(rank);
		}
		
		sortDoubleString (rankValues); // decreasing ...
		
		for (int i = 0; i < rankValues.size(); i++)
			temp.put(rankValues.get(i), ranks.get(rankValues.get(i)));
		
		ranks.clear();
		ranks.putAll(temp);
	}
	
	private static void sortDoubleString (List<String> rankValues){ // decreasing ...
		boolean exchange;// = false;
		int n = rankValues.size();		
		
		do {
			exchange = false;
			for (int i = 0 ; i < (n - 1); i++){
				if (Double.valueOf(rankValues.get(i)).doubleValue() < Double.valueOf(rankValues.get(i+1)).doubleValue()){					
					String tampon = rankValues.get(i);
					rankValues.set(i, rankValues.get(i+1));
					rankValues.set(i+1, tampon);
					exchange = true;
				}
			}
			n--;
		} while (exchange);
	}
	
	private static List<MCD> getMCDs(DatalogQuery view,  Predicate subGoal, DatalogQuery query){
		
		if (subGoal.isMCDProcessed(view))   // the MCDs for this view and subgoal are already processed 
			return subGoal.getMCDsRelatedToView(view);
		//else
		
		List<MCD> mcds = new ArrayList<MCD> ();
				
		List<MCDMappings> mappings = createMapping(subGoal, view);

		// for every mapping created check whether properties are
		// fulfilled
		for (MCDMappings map : mappings) {
			
			// create MCD
			MCD mcd = new MCD(subGoal, query, view, map);

			// MCD can be extend to fulfill properties
			if (mcd.fulfillProperty()) {
				mcds.add(mcd);
			}
		}
		
		subGoal.setMCDsRelatedToView(view, mcds);
		
		return mcds;
	}
	
	private static List<MCD> getMCDs (List<DatalogQuery> views, Predicate subGoal, DatalogQuery query){
		List<MCD> mcds = new ArrayList<MCD>();

		for (DatalogQuery view : views){			
			List<MCD> list = getMCDs(view, subGoal, query); 
			mcds.addAll(list);
		}
		return mcds;
	}
	/**
	 * Called by formMCDs. The given query subgoal is tested if it can be mapped
	 * to every predicate of the view. If a mapping is possible, a new mapping
	 * object is added to the list of mappings.
	 * 
	 * @param subgoal
	 *            current query subgoal
	 * @param view
	 *            current view
	 * @return list of possible mappings
	 */
	private static List<MCDMappings> createMapping(Predicate subgoal, DatalogQuery view) {
		List<Predicate> viewPredicates = view.getPredicates();
		List<MCDMappings> mappings = new ArrayList<MCDMappings>();

		for (Predicate viewPred : viewPredicates) {

			if (subgoal.canBeMapped(viewPred)) {
				mappings.add(new MCDMappings(subgoal, viewPred));
			}
		}
		return mappings;
	}
	
	private static boolean isRewriting(List<MCD> mcds, DatalogQuery query) {
		int countPredicates = 0;

		for (MCD mcd : mcds) {
			countPredicates += mcd.numberOfSubgoals();
		}

		// compare total number of predicates with number of query subgoals
		if (countPredicates != query.numberOfPredicates()) {
			return false;
		}

		// test pairwise disjoint
		for (int i = 0; i < mcds.size(); i++) {
			for (int j = 0; j < mcds.size(); j++) {
				if (i != j) {
					MCD mcd1 = mcds.get(i);
					MCD mcd2 = mcds.get(j);
					if (!mcd1.isDisjoint(mcd2)) {
						return false;
					}
				}
			}
		}

		// x exists in C1 and C2 ==> it must be mapped to the same constant
		for (int i = 0; i < mcds.size(); i++) {
			MCD mcd1 = mcds.get(i);
			Mapping constMap1 = mcd1.mappings.constMap;
			for (int j = 0; j < mcds.size(); j++) {
				if (i != j) {
					MCD mcd2 = mcds.get(j);
					Mapping constMap2 = mcd2.mappings.constMap;
					for (PredicateElement elem : constMap1.arguments) {
						if ((constMap2.containsArgument(elem) && !(constMap1
								.getFirstMatchingValue(elem).equals(constMap2
								.getFirstMatchingValue(elem))))) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static List<MCD> removeDuplicates(List<MCD> mcds) {
		List<MCD> noDuplicates = new ArrayList<MCD>();

		for (MCD mcd : mcds) {
			boolean contains = false;

			for (MCD noDup : noDuplicates) {
				if (mcd.equals(noDup)) {
					contains = true;
				}
			}
			if (!contains) {
				noDuplicates.add(mcd);
			}
		}
		return noDuplicates;
	}

}
