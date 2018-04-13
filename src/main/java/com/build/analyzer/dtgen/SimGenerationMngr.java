package com.build.analyzer.dtgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.keyword.Keyword;
import com.build.keyword.TermExtractor;
import com.build.metrics.RankingCalculator;
import com.build.revertanalyzer.ReverAnalyzer;
import com.buildlogparser.logmapper.BuildErrorLogMapper;
import com.github.gumtreediff.actions.model.Action;

public class SimGenerationMngr {

	public void simAnalyzerFullLog() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric=new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();			
			project = project.replace('/', '@');
			
			System.out.println(project);
			
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, true);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getPassFilelist();

			String[] actualfixs = actualfixfile.split(";");	

					
			projects.get(index).setTotalfileCount(sortedsimmap.size());			
			int topn=rankmetric.getTopN(keys, actualfixs);
			double mrr=rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map=rankmetric.getMeanAveragePrecision(keys, actualfixs);
			
			projects.get(index).setFulllogPos(topn);
			projects.get(index).setFulllogMrr(mrr);
			projects.get(index).setFulllogMap(map);
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public void simAnalyzerFilteredLog() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric=new RankingCalculator();
		
		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			
			System.out.println(project);
			// project="D:\\test\\appsly-android-rest";
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			int lastindex = 0;

			for (int fileindex = 0; fileindex < actualfixs.length; fileindex++) {
				for (int tindex = 0; tindex < keys.size(); tindex++) {
					String file = keys.get(tindex);

					if (file.equals(actualfixs[fileindex])) {
						if (lastindex < tindex) {
							lastindex = tindex;
						}

					}
				}

			}

			projects.get(index).setFilterlogdualPos(lastindex);
		
			double mrr=rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map=rankmetric.getMeanAveragePrecision(keys, actualfixs);
			
			projects.get(index).setFilterlogdualMrr(mrr);
			projects.get(index).setFilterlogdualMap(map);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}
	
	
	public void simAnalyzerDifferemtialLog() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric=new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();			
			project = project.replace('/', '@');
			
			System.out.println(project);
			
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getPassFilelist();

			String[] actualfixs = actualfixfile.split(";");			

			int topn=rankmetric.getTopN(keys, actualfixs);
			double mrr=rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map=rankmetric.getMeanAveragePrecision(keys, actualfixs);
			
			projects.get(index).setFilterlogPos(topn);
			projects.get(index).setFilterlogMrr(mrr);
			projects.get(index).setFilterlogMap(map);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}
	
	public void simAnalyzerDifferemtialLogWithChange() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric=new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();			
			project = project.replace('/', '@');
			
			System.out.println(project);
			
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false);

			

			String actualfixfile = proj.getPassFilelist();
			String failintrofiles = proj.getFailFilelist();

			String[] actualfixs = actualfixfile.split(";");		
			String[] failfixs = failintrofiles.split(";");
			
			
			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;

				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 0.2 * simmap.get(name);
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}
			
			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn=rankmetric.getTopN(keys, actualfixs);
			double mrr=rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map=rankmetric.getMeanAveragePrecision(keys, actualfixs);
			
			projects.get(index).setFilterlogdualPos(topn);
			projects.get(index).setFilterlogdualMrr(mrr);
			projects.get(index).setFilterlogdualMap(map);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	
	
	public void simtesting() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric=new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();			
			project = project.replace('/', '@');
			
			System.out.println(project);
			
			String logtext=proj.getBlLargelog();
			
			List<Keyword> keywords=TermExtractor.guessFromString("getValue textValue");
			
			for(int in=0;in<keywords.size();in++)
			{
				System.out.println(keywords.get(in).getStem()+"->"+keywords.get(in).getFrequency());
			}
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;

	}
	
	public String getAllContent(List<Keyword> keywords)
	{
		
		StringBuilder strbuilder=new StringBuilder();
		
		for(int in=0;in<keywords.size();in++)
		{
			strbuilder.append(keywords.get(in).getStem());
			strbuilder.append(" ");			
		}
		
		return strbuilder.toString();
		
	}

}
