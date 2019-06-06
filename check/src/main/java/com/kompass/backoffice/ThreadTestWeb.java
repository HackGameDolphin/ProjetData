package com.kompass.backoffice;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;

//import tools.Log;

public class ThreadTestWeb implements Callable<String[]> {

	private String url = null;
	private String kid = null;

	public ThreadTestWeb(String kid, String url) {
		this.url = url;
		this.kid = kid;
	}

	@Override
	public String[] call() {
		String[] nextLine = new String[4];
		nextLine[0] = kid;
		nextLine[1] = url;
		HashSet<String> emails = new HashSet<>();
		//Log.logg("### " + Thread.currentThread().getName() + " Traitement de : " + url + " pour " + kid);
		String res = testPages(url);
		int posSep = res.indexOf(":");
		nextLine[2] = res;
		if (posSep > 2) {
			nextLine[2] = res.substring(0, posSep);
			nextLine[3] = res.substring(posSep + 1);
		}
		return nextLine;

	}

	public static String testPages(String url) {
		class InterceptWebConnection extends FalsifyingWebConnection {
			int nb = 0;

			public InterceptWebConnection(WebClient webClient) throws IllegalArgumentException {
				super(webClient);
			}

			@Override
			public WebResponse getResponse(WebRequest request) throws IOException {
				WebResponse response = super.getResponse(request);
				nb++;
				String[] repous = { "syndication.twimg", "livechatinc", "static.ads-twitter.com", "analytics", "googletagmanager.com",
						"snap.licdn.com", "cdn.mxpnl.com", "chartbeat", "parsely", "sail-horizon", "kameleoon", "apis.google",
						"securepubads", "crypto-js", "avis-verifies.com" };
				if (nb > 300) {
					Log.logg("!!!!!!!!!!!!!!!!!!!! nb " + nb + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + response.getWebRequest().getUrl().toString().toLowerCase());
					return createWebResponse(response.getWebRequest(), "", "application/javascript", 200, "Ok");
				}
				for (String sRepou : repous) {
					if (response.getWebRequest().getUrl().toString().toLowerCase().contains(sRepou.toLowerCase())) {
						return createWebResponse(response.getWebRequest(), "", "application/javascript", 200, "Ok");
					}
				}
				return super.getResponse(request);
			}
		}

		String htmlPage = null;
		String textPage = null;
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		Log.logg("test de " + url + " (" + webClient.getBrowserVersion().getNickname()
				+ " " + webClient.getBrowserVersion().getBrowserVersionNumeric() + ")");

		try {
			/*webClient.setJavaScriptTimeout(10000);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setDownloadImages(false);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setTimeout(20000);
			webClient.getOptions().setPopupBlockerEnabled(true);
			webClient.getOptions().setHistoryPageCacheLimit(100);
			
			InterceptWebConnection lala = new InterceptWebConnection(webClient);
			//System.setProperty("phantomjs.binary.path", "C:\\tools\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
			//WebDriver ghostDriver = new PhantomJSDriver();
			//ghostDriver.get(url);
			//Document docGhost = Jsoup.parse(ghostDriver.getPageSource());
			//matcher = p.matcher(docGhost.text());
			HtmlPage page = webClient.getPage(url);*/
			//fw_outE.write("\n #!!!! " + url + "\n" + page.asXml() + "\n");
			Document doc = Jsoup.connect(url)
					.followRedirects(true)
					.timeout(120000)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0")
					.get();
			/*htmlPage = page.asXml();
			textPage = page.asText();
			webClient.close();
			lala.close();*/
			htmlPage = doc.text();
			textPage = doc.text();
			//if (htmlPage == null || htmlPage.length() < 50 || textPage.trim().length() == 0)
			//	return "empty page";

			String[] errors = { "404", "403", "402", "401", "500", "501", "502", "503", "504", "error", "Site not installed", "Not Found",
					"Object not found", "Unauthorized Access" };
			for (String error : errors) {
				if (textPage.trim().toLowerCase().startsWith(error.toLowerCase()))
					return error;

			}
			//ghostDriver.quit();
		} catch (Exception e) {
			//Log.logg("!!!!!!!!!!!!!!! prout !!!!!!!!!!!!!");
			//Log.logg(e.toString());
			return e.toString();
		} finally {
			webClient.close();
		}
		//Log.logg(url + " | " + textPage);
		return "ok";
	}
}
