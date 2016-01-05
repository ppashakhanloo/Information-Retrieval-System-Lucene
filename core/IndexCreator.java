package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexCreator {

	private IndexWriter writer;
	private FileInputStream fis;
	private BufferedReader br;

	public IndexWriter getIndex() {
		return writer;
	}

	public IndexCreator(String dir, String docPath) throws Exception {
		fis = new FileInputStream(new File(docPath));
		br = new BufferedReader(new InputStreamReader(fis));
		Directory indexDir = FSDirectory.open((new File(dir)).toPath());
		Analyzer analyzer = new MyAnalyzer();
		IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
		cfg.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(indexDir, cfg);
		understandCISIcollection();
	}

	private void understandCISIcollection() throws Exception {

		String line = null;
		String moreThanOneLine = "";
		line = br.readLine();

		// read one doc per iteration of the loop
		while (true) {
			Document doc = new Document();
			moreThanOneLine = "";

			// Read id
			doc.add(new StringField("docID", line.split(" ")[1],
					Field.Store.YES));
			line = br.readLine();

			// Read title
			while (!line.startsWith(".A")) {
				line = br.readLine();
				if (!line.startsWith(".A"))
					moreThanOneLine += line + "\n";
			}
			doc.add(new TextField("docTitle", moreThanOneLine, Field.Store.YES));

			// Read authors
			moreThanOneLine = "";
			while (!line.startsWith(".W")) {
				line = br.readLine();
				if (!line.startsWith(".W"))
					moreThanOneLine += line + "\n";
			}
			doc.add(new TextField("docAuthors", moreThanOneLine,
					Field.Store.YES));

			// Read content
			moreThanOneLine = "";
			if (line != null)
				while (!line.startsWith(".X") && line != null) {
					line = br.readLine();
					if (!line.startsWith(".X"))
						moreThanOneLine += line + "\n";
				}
			doc.add(new TextField("docContent", moreThanOneLine,
					Field.Store.YES));

			// Skip .X
			if (line != null)
				while (!line.startsWith(".I")) {
					line = br.readLine();
					if (line == null)
						break;
				}
			writer.addDocument(doc);

			if (line == null)
				break;
		}

		close();
	}

	private void close() throws IOException {
		writer.close();
	}
}