package com.kompass.backoffice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class ThreadCheckUrl implements Callable<String[]> {

	private String url = null;
	private String kid = null;

	public ThreadCheckUrl(String kid, String url) {
		this.url = url;
		this.kid = kid;
	}

	@Override
	public String[] call() {
		boolean ok2 = false;
		boolean isOk = testURL(kid, url, null, 0);
		String[] res = new String[3];
		res[0] = kid;
		res[1] = url;
		res[2] = "" + isOk;
		// if (!testURL(kid, url, writer, null, 0))
		// ok2 = testURL2(kid, url, writer, null, 0);
		// if (ok2) {
		// System.err.println("UNIT!! " + url);
		// writer.writeNext(new String[] { kid, url, "200", "##UIT##" });
		// }
		return res;
	}

	private static boolean testURL(String kid, String url, String method, int nested) {
		HttpURLConnection conn;
		String location;
		URL base;
		URL next;
		if (nested > 5) {
			Log.logg("nested > 5 for " + kid + ": " + url);
			return false;
		}
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod((method == null) ? "HEAD" : method);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(20000);
			conn.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(true);
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
			conn.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			conn.setRequestProperty("accept-encoding", "gzip, deflate, br");
			conn.setRequestProperty("accept-language", "en-US,en;q=0.8");

			boolean ok = false;
			switch (conn.getResponseCode()) {

				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_SEE_OTHER:
				case 307:
				case HttpURLConnection.HTTP_MOVED_TEMP:
					location = conn.getHeaderField("Location");
					base = new URL(url);
					next = new URL(base, location); // Deal with relative URLs
					//Log.log("redirecting " + url + " to " + next.toExternalForm());
					url = next.toExternalForm();
					ok = testURL(kid, url, null, nested + 1);
					break;

				// 405 and similar errors might be becaues of HEAD request =>
				// try
				// with normal GET instead
				case HttpURLConnection.HTTP_BAD_METHOD:
				case HttpURLConnection.HTTP_INTERNAL_ERROR:
				case HttpURLConnection.HTTP_BAD_REQUEST:
				case HttpURLConnection.HTTP_FORBIDDEN:
				case HttpURLConnection.HTTP_UNAVAILABLE:
				case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
					//Log.log("bad method " + url);
					ok = testURL(kid, url, "GET", nested + 1);
					break;

				case 429: // too many requests. slow down a bit and try again
					Thread.sleep(3000 * nested);
					ok = testURL(kid, url, null, nested + 1);
					break;

				case HttpURLConnection.HTTP_OK:
					ok = true;
					break;
			}

			//Log.logg(url);
			String ip = InetAddress.getByName(conn.getURL().getHost()).getHostAddress();
			Log.logg(kid + " ; " + ip + " ; Response Code: " + conn.getResponseCode() + "Response Message: "
					+ conn.getResponseMessage());
			if (nested == 0 && !ok) {
				ok = testURL2(kid, url, null, 0);
				if (ok)
					return true;
			}

			return ok;
		} catch (Exception e) {
			//Log.logErr(url);
			//Log.logErr("Error: " + e.getMessage());
			return false;
		}
	}

	private static boolean testURL2(String kid, String url, String method, int nested) {
		WebClient webClient = new WebClient();
		webClient.setJavaScriptTimeout(10000);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setTimeout(20000);
		webClient.getOptions().setPopupBlockerEnabled(true);
		webClient.getOptions().setHistoryPageCacheLimit(100);
		System.out.println("now testing " + url);
		HtmlPage pp = null;
		try {
			pp = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// return false;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// return false;
		}

		webClient.close();
		if (pp != null && pp.isHtmlPage() && pp.getWebResponse().getStatusCode() == 200) {
			return true;
		}
		return false;
	}
}
