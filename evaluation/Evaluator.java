package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

import search.Searcher;

public class Evaluator {
	private FileInputStream fis;
	private BufferedReader br;

	private TreeMap<Integer, String> CISIqueries = new TreeMap<Integer, String>();
	private TreeMap<Integer, ArrayList<Integer>> CISIrel = new TreeMap<Integer, ArrayList<Integer>>();

	private String queriesPath;
	private String relPath;
	private double BETA = 1.0;
	private Searcher searcher;

	public Evaluator(String queriesPath, String relPath, Searcher searcher)
			throws IOException {
		this.queriesPath = queriesPath;
		this.relPath = relPath;
		this.searcher = searcher;
		understandCISIqueries();
		understandCISIrel();
	}

	private void understandCISIrel() throws IOException {
		fis = new FileInputStream(new File(relPath));
		br = new BufferedReader(new InputStreamReader(fis));

		String line = br.readLine();

		while (line != null) {
			line = line.trim().replaceAll(" +", " ").replace("\t", " ");

			if (CISIrel.containsKey(Integer.parseInt(line.split(" ")[0]))) {
				CISIrel.get(Integer.parseInt(line.split(" ")[0])).add(
						Integer.parseInt(line.split(" ")[1]));
			} else {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(Integer.parseInt(line.split(" ")[1]));
				CISIrel.put(Integer.parseInt(line.split(" ")[0]), temp);
			}
			line = br.readLine();
		}

		fis.close();
		br.close();
	}

	private void understandCISIqueries() throws IOException {
		fis = new FileInputStream(new File(queriesPath));
		br = new BufferedReader(new InputStreamReader(fis));
		String line = br.readLine();
		String moreThanOneLine = "";
		String consQuery = "";
		int docID = 0;
		while (line != null) {
			if (line.startsWith(".I")) {
				consQuery = "";
				docID = Integer.parseInt(line.split(" ")[1]);
				line = br.readLine();
				if (line.startsWith(".T")) {
					moreThanOneLine = "";
					while (!line.startsWith(".A") && !line.startsWith(".W")) {
						line = br.readLine();
						if (!line.startsWith(".A") && !line.startsWith(".W"))
							moreThanOneLine += line + " ";
					}
					consQuery += "docTitle:(" + moreThanOneLine + ") OR ";
					// consQuery += moreThanOneLine + " ";
				}
				if (line.startsWith(".A")) {
					moreThanOneLine = "";
					while (!line.startsWith(".W")) {
						line = br.readLine();
						if (!line.startsWith(".W"))
							moreThanOneLine += line + " ";
					}
					consQuery += "docAuthors:(" + moreThanOneLine + ") OR ";
					// consQuery += moreThanOneLine + " ";
				}
				if (line.startsWith(".W")) {
					moreThanOneLine = "";
					while (line != null && !line.startsWith(".I")) {
						line = br.readLine();
						if (line == null)
							break;
						if (!line.startsWith(".I"))
							moreThanOneLine += line + " ";
					}
					consQuery += "docContent:(" + moreThanOneLine + ") OR ";
					// consQuery += moreThanOneLine + " ";
				}
			} else {
				line = br.readLine();
			}
			if (consQuery.endsWith("OR "))
				consQuery = consQuery.substring(0, consQuery.length() - 4);

			CISIqueries.put(docID, consQuery);
		}
		fis.close();
		br.close();
	}

	public TreeMap<Integer, Double> computeMAP() throws ParseException,
			IOException {
		TreeMap<Integer, Double> res = new TreeMap<Integer, Double>();
		double relret = 0;
		double interresult = 0;
		double rets = 0;
		for (Integer qIDrel : CISIrel.keySet()) {
			relret = 0;
			interresult = 0;
			rets = 0;
			ArrayList<Document> docs = searcher.searchStandardQuery(CISIqueries
					.get(qIDrel));
			for (int j = 0; j < docs.size(); j++) {
				// check if it is relevant!
				if (CISIrel.get(qIDrel).contains(
						Integer.parseInt(docs.get(j).get("docID")))) {
					// compute precision
					relret++;
					rets++;
					interresult += (relret * 1.0) / (rets * 1.0);
				}
			}

			interresult /= CISIrel.get(qIDrel).size();
			res.put(qIDrel, interresult);
		}

		double finres = 0;
		for (Integer i : res.keySet()) {
			finres += res.get(i);
		}
		finres = finres / (double) res.size();

		res.put(-1, finres);

		return res;
	}

	public TreeMap<Integer, Double> computeFMeasure() throws IOException,
			ParseException {
		TreeMap<Integer, Double> res = new TreeMap<Integer, Double>();

		double REL = 0;
		double relRet = 0;
		double recall = 0;
		double precision = 0;
		for (Integer qIDrel : CISIrel.keySet()) {
			REL = CISIrel.get(qIDrel).size();
			relRet = 0;
			ArrayList<Document> docs = searcher.searchStandardQuery(CISIqueries
					.get(qIDrel));
			for (int i = 0; i < docs.size(); i++) {
				if (CISIrel.get(qIDrel).contains(
						Integer.parseInt(docs.get(i).get("docID")))) {
					relRet++;
				}
			}

			recall = relRet / REL;
			precision = relRet / docs.size();

			res.put(qIDrel, computeF(precision, recall, BETA));
		}

		double finres = 0;
		for (Integer i : res.keySet()) {
			finres += res.get(i);
		}
		finres = finres / (double) res.size();

		res.put(-1, finres);

		return res;
	}

	private double computeF(double precision, double recall, double BETA) {
		double res = ((BETA * BETA + 1.0) * precision * recall)
				/ ((BETA * BETA * precision) + recall);
		if (new Double(res).isNaN())
			return 0;
		return res;
	}
}
