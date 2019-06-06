package com.kompass.backoffice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


public class TestSites {
	static int nbCrawled = 0;

	int nbComps = 30000;
	int nbThreads = 40;

	int nbRead = 0;

	public static void main(String[] args) {
		TestSites leTraitement = new TestSites();
		String name = null;
		if (args.length > 0 && args[0].length() > 4)
			name = args[0];
		leTraitement.traitement(name);
	}

	public void traitement(String fileName) {
		Log.logg(" début de " + Thread.currentThread().getStackTrace()[1].getClassName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
		long begin = System.currentTimeMillis();
		try {

			String wDir = "";
			if (System.getProperty("os.name").trim().startsWith("Windows")) {
				wDir = "C:\\Users\\peepa\\Documents\\docs\\extracts\\web\\ES\\";
			} else
				wDir = "./";

			String name = wDir + "companies-test.csv";
			if (fileName != null)
				name = wDir + fileName;
			int posPoint = name.lastIndexOf('.');
			String nameOut = name.substring(0, posPoint) + "_out" + ".txt";

			Log.logg(" traitement de " + name);

			FileInputStream input = new FileInputStream(new File(name));
			CharsetDecoder decoder = Charset.forName(StandardCharsets.UTF_8.name()).newDecoder();
			decoder.onMalformedInput(CodingErrorAction.IGNORE);
			InputStreamReader iReader = new InputStreamReader(input, decoder);
			BufferedReader bufferedReader = new BufferedReader(iReader);
			// CSVReader reader = new CSVReader(new FileReader(name), '~');
			//CSVReader reader = new CSVReader(bufferedReader, '\t', '\0');
			CSVReader reader = new CSVReader(bufferedReader, '\t');

			String[] nextLine = {};

			BufferedWriter fw_out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nameOut), "UTF-8"));
			CSVWriter leWriter = new CSVWriter(fw_out, '\t');
			int nbtot = 0;
			int nb = 0;
			List<String[]> listLignes = new ArrayList<>();
			while ((nextLine = reader.readNext()) != null) {
				nb++;
				nbRead++;
				listLignes.add(nextLine);

				if (listLignes.size() == nbComps) {
					nbCrawled += listLignes.size();
					traiteLignes(listLignes, leWriter);
					leWriter.flush();
					Log.logg("#### crawled : " + nbCrawled);
				}
			}
			nbCrawled += listLignes.size();
			if (listLignes.size() > 0)
				traiteLignes(listLignes, leWriter);
			Log.logg("#### crawled : " + nbCrawled);
			leWriter.close();
			reader.close();
		} catch (Exception e) {
			Log.logg(e);
		}
		Log.logg("!!!!!!!!!!!! Fin traitement " + " !!!!!!!!!!!!!!!");
	}

	private void traiteLignes(List<String[]> listLignes, CSVWriter leWriter) {
		Log.logg("!!!!!!!!!!!! Debut traitement de " + listLignes.size() + " !!!!!!!!!!!!!!!");
		ExecutorService pool = Executors.newFixedThreadPool(nbThreads);
		ExecutorCompletionService<String[]> completionService = new ExecutorCompletionService<>(pool);

		//List<Future<List<String[]>>> lFuture = new ArrayList<>();
		for (String[] laLigne : listLignes) {
			//lFuture.add(pool.submit(new CrawlEmails(laLigne[0], laLigne[1])));
			completionService.submit(new ThreadTestWeb(laLigne[0], laLigne[1]));
			//completionService.submit(new ThreadCheckUrl(laLigne[0], laLigne[1]));
		}
		int nb2Wait = listLignes.size();
		listLignes.clear();
		pool.shutdown();
		System.gc();
		for (int i = 0; i < nb2Wait; i++) {
			try {
				//for (String[] laLigne : completionService.take().get(30, TimeUnit.MINUTES)) {
				Future<String[]> laLigneF = completionService.poll(5, TimeUnit.MINUTES);
				if (laLigneF != null) {
					String[] laLigne = laLigneF.get(31, TimeUnit.MINUTES);
					if (laLigne != null)
						leWriter.writeNext(laLigne);
					try {
						leWriter.flush();
					} catch (IOException e) {
						Log.logg(e);
					}
				} else {
					Log.logg("waited to long");
				}
				Log.logg("$$$$ nbRead " + nbRead + " reste " + (nb2Wait - i));
			} catch (InterruptedException e) {
				Log.logg(e);
			} catch (ExecutionException e) {
				Log.logg(e);
			} catch (TimeoutException e) {
				Log.logg(e);
				Log.logg("$$$$$ Trop long on passe à la suite");
			}
		}
		Log.logg("!!!!!!!!!!!! Fin Threads " + " !!!!!!!!!!!!!!!");

	}

}
