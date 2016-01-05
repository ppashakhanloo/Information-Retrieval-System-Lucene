package ui;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.document.Document;

import search.Searcher;
import core.IndexCreator;
import evaluation.Evaluator;

public class Main {
	public static void main(String[] args) throws Exception {
		IndexCreator index = null;
		Searcher searcher = null;
		Scanner scan = new Scanner(System.in);
		Evaluator evaluator = null;
		String input = "";
		showMainMenu();
		MAIN: while (true) {
			input = scan.nextLine();
			switch (input) {
			case "1":
				System.out
						.println("Enter the directory for the index: [or none for default]");
				String path1 = scan.nextLine();
				if (path1.equals(""))
					path1 = "C:\\Users\\Pardis\\workspace\\MIR_PROJECT2_PART1\\";
				System.out
						.println("Enter the doc collection file to be indexed: [or none for default]");
				String path2 = scan.nextLine();
				if (path2.equals(""))
					path2 = "C:\\Users\\Pardis\\workspace\\MIR_PROJECT2_PART1\\CISI\\CISI.ALL";
				index = new IndexCreator(path1, path2);
				System.out.println("Index created successfully.");
				break;
			case "2":
				// get query and search
				if (index != null) {
					searcher = new Searcher(index.getIndex().getDirectory());
					System.out.println("Enter your query:");
					System.out
							.println("[Sample: docTitle:\"Hello All!\" AND docAuthors:\"The Weather!\" AND docContent:\"Wow! the best search engine ever!\"]");
					showFormatted(searcher.searchStandardQuery(scan.nextLine()));
				} else
					System.out.println("Please initialize the index first.");

				break;
			case "3":
				// evaluate
				if (index != null) {
					if (searcher == null)
						searcher = new Searcher(index.getIndex().getDirectory());

					System.out
							.println("Enter the queries file to be evaluated: [or none for default]");
					path1 = scan.nextLine();
					if (path1.equals(""))
						path1 = "C:\\Users\\Pardis\\workspace\\MIR_PROJECT2_PART1\\CISI\\CISI.QRY";

					System.out
							.println("Enter the relevance file to be evaluated: [or none for default]");
					path2 = scan.nextLine();
					if (path2.equals(""))
						path2 = "C:\\Users\\Pardis\\workspace\\MIR_PROJECT2_PART1\\CISI\\CISI.REL";
					evaluator = new Evaluator(path1, path2, searcher);

					EVAL: while (true) {
						TreeMap<Integer, Double> evalRes;

						showEvalMenu();
						String meas = scan.nextLine();
						if (meas.equals("1")) {
							evalRes = evaluator.computeMAP();

							for (Integer i : evalRes.keySet()) {
								if (i != -1)
									System.out.println("MAP(Q#" + i + ")="
											+ evalRes.get(i));
							}
							System.out.println("-----------------------");
							System.out.println("MAP(ALL)=" + evalRes.get(-1));
						} else if (meas.equals("2")) {
							evalRes = evaluator.computeFMeasure();

							for (Integer i : evalRes.keySet()) {
								if (i != -1)
									System.out.println("F1(Q#" + i + ")="
											+ evalRes.get(i));
							}
							System.out.println("-----------------------");
							System.out.println("F1(ALL)=" + evalRes.get(-1));
						} else if (meas.equals("q")) {
							break EVAL;
						} else {
							System.out.println("Input not valid.");
						}
					}

				} else {
					System.out.println("Please initialize the index first.");
				}

				break;
			case "q":
				// quit
				scan.close();
				break MAIN;
			default:
				System.out.println("Enter valid input.");
			}
			showMainMenu();
		}
	}

	private static void showMainMenu() {
		System.out.println("|-------------------------------|");
		System.out.println("|Choose One of the Sections:\t|");
		System.out.println("|\t1: Create the Index\t|");
		System.out.println("|\t2: Search\t\t|");
		System.out.println("|\t3: Evaluate\t\t|");
		System.out.println("|\tq: Quit\t\t\t|");
		System.out.println("|-------------------------------|");
		System.out.print("$");
	}

	private static void showEvalMenu() {
		System.out.println("|-------------------------------|");
		System.out.println("|Choose One of the Sections:\t|");
		System.out.println("|\t1: MAP\t\t\t|");
		System.out.println("|\t2: F-Measure\t\t|");
		System.out.println("|\tq: Back\t\t\t|");
		System.out.println("|-------------------------------|");
		System.out.print("$");
	}

	private static void showFormatted(ArrayList<Document> docs) {
		for (int i = 0; i < docs.size(); i++) {
			System.out.print(docs.get(i).get("docID") + "\t\t");
			if ((i + 1) % 5 == 0)
				System.out.println();
		}
		System.out.println();
	}
}
