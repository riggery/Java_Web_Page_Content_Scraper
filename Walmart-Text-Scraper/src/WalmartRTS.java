import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author Riggery
 *
 */
public class WalmartRTS {
	/**
	 * 
	 * @param url
	 * 		Pass target source address in String format			
	 * @return
	 * 		Return page content in String
	 * @throws IOException
	 * 	 	Catch connection exception 
	 */
	public String getConnection(String url) throws IOException{
		HttpURLConnection conn = null;
		BufferedReader rd  = null;
		StringBuilder sb = null;
		String line = null;
		String jsp = null;
		try{
			URL obj = new URL(url);
			conn = null;
			conn = (HttpURLConnection) obj.openConnection();

			// Set up a request.
			conn.setConnectTimeout( 10000 );    // 10 sec
			conn.setReadTimeout( 10000 );       // 10 sec
			//conn.setInstanceFollowRedirects( true );
			conn.setRequestProperty( "User-agent", "spider" );
			conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
			conn.setRequestMethod("GET");

			conn.connect();

			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
			sb = new StringBuilder();

			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}


			jsp = sb.toString().trim();
			return jsp;

		}catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (ProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		finally
		{
			//close the connection, set all objects to null
			conn.disconnect();
			rd = null;
			sb = null;
			conn = null;
		}
	}


	/**
	 * Get total number of search results
	 * The total number of items is shown in javascript, cannot parse use jsoup
	 * @param keywords
	 * 		Search keywords 
	 * @return
	 * 		return total number in String 
	 * @throws IOException
	 */
	public String queryOne(String keywords) throws IOException {
		/* replace any space between keywords by "%20" */
		keywords = keywords.replace(" ", "%20");
		String total = "";
		String url = "http://www.walmart.com/search/search-ng.do?search_constraint=0&Find=Find&_ta=1&search_query="
				+ keywords;
		//System.out.println(url);
		String jsp = getConnection(url);
		Document doc = Jsoup.parse(jsp);
		Elements scripts = doc.select("script");
		for(Element script:scripts){

			String[] parts  = script.html().split("s_omni.prop");
			for(int i=0;i<parts.length;i++){
				//System.out.println("**********************************************************************"+i);
				if(parts[i].startsWith("16=")){
					total = parts[i];
					//System.out.println(s);
					total = total.substring(total.indexOf("\"") + 1);
					total = total.substring(0, total.indexOf("\""));
					//System.out.println("Total num of Results:"+toatal);
					return total;
				}
			}
		}
		if (jsp == null) {
			return "Failed to retrieve web page.";
		}
		return total;
	}
	
	
	/**
	 * Add result objects in ArrayList, show in console log
	 * @param doc
	 * @param number
	 */
	public void printInConsole(Document doc,int number){
		ArrayList<ResultObject> result_obj_list = new ArrayList<ResultObject>();
		Elements products = doc.getElementsByClass("prodInfoBox");
		if(products.size()==0){
			System.out.println("No more results");
		}else{
			System.out.println("show result on page "+number);
			for(Element product : products){
				Element target = product.getElementsByTag("a").first();
				Element targetprice = product.select("div[class=camelPrice]").first();
				//System.out.println(targetprice);
				String price_start;
				String price_end;
				try{
					price_start = targetprice.select(".bigPriceText2").first().text() + targetprice.select(".smallPriceText2").first().text();
				}catch(Exception e){
					try{
						price_start = targetprice.select(".bigPriceTextOutStock2").first().text() + targetprice.select(".smallPriceTextOutStock2").first().text();
					}catch(Exception err){
						price_start = "Not price";
					}
				}
				try{
					price_end = targetprice.select(".bigPriceText2").get(1).text() + targetprice.select(".smallPriceText2").get(1).text();
				}catch(Exception e){
					try{
						price_end = targetprice.select(".bigPriceTextOutStock2").get(1).text() + targetprice.select(".smallPriceTextOutStock2").get(1).text();
					}catch(Exception err){
						price_end = "";
					}
				}
				String price = price_start;
				if(!price_end.isEmpty()){
					price = price + " ~ " + price_end;
				}
				String title = target.attr("title");
				ResultObject result_obj = new ResultObject(title, price);
				result_obj_list.add(result_obj);
//				System.out.print(target.attr("title"));
//				System.out.print("===========================>");
//				System.out.println(price);
			}
			Iterator<ResultObject> ir = result_obj_list.iterator();
			while(ir.hasNext()){
				ResultObject cur = ir.next();
				System.out.println(cur.getTitle()+"===========================>"+cur.getPrice());
			}
		}
	}


	/**
	 * Simple redirect page by using &ic= in request link
	 * @param doc
	 * @param keywords
	 * @param number
	 * @throws IOException
	 */
	public void redirectPageSimple(Document doc, String keywords, int number) throws IOException{
		String postfix;
		if(number<1){
			System.out.println("Invalid Page Number");
			return;
		}else{
			postfix = "&search_query=" + keywords + "&ic=16_"+16*(number-1);
			//System.out.println(postfix);
		}
		String url = "http://www.walmart.com/search/search-ng.do?search_constraint=0&Find=Find&_ta=1&search_query="
				+ keywords + "&" + postfix;
		String jsp = getConnection(url);
		Document redirect_doc = Jsoup.parse(jsp);
		printInConsole(redirect_doc, number);
	}


	/**
	 * Redirect page by using DOM operation 
	 * @param doc
	 * @param keywords
	 * @param number
	 * @throws IOException
	 */
	public void redirectPage(Document doc, String keywords, int number) throws IOException{
		/*DOM operation*/
		Element page_div = doc.getElementById("bottomPagination");
		Element page_div_ul = page_div.select("ul").first();
		Element cur_min_page = page_div_ul.select("li").first();
		/*Skip Previous and ... button. Find out max and min page number of current page*/
		if(cur_min_page.text().equals("Previous")){
			cur_min_page =cur_min_page.nextElementSibling();
		}
		if(cur_min_page.text().equals("...")){
			cur_min_page =cur_min_page.nextElementSibling();
		}
		int cur_min_page_num = Integer.parseInt(cur_min_page.text());
		int cur_max_page_num = Integer.parseInt(page_div_ul.select("li").last().previousElementSibling().previousElementSibling().text());
		Elements page_lis = page_div_ul.select("li");
		//System.out.println(cur_min_page_num+" "+cur_max_page_num);
		/*
		  Recursion to redirect to target page
		  Base case: target page number is between max min page number,Then call printInConsole
		 */
		if(number<1){
			System.out.println("Invalid Page Number");
			return;
		}else if(number>cur_max_page_num){    /*redirect to Next 5 pages*/
			String next_link = page_div_ul.select("li").last().previousElementSibling().select("a").first().attr("href");
			String url = "http://www.walmart.com/search/search-ng.do?search_constraint=0&Find=Find&_ta=1&search_query="
					+ keywords + "&" + next_link;
			String jsp = getConnection(url);
			Document redirect_doc = Jsoup.parse(jsp);
			redirectPage(redirect_doc, keywords, number);          
		}else{
			String postfix = "";
			if((number-1)%5==0){
				postfix = "?tab_value=all&search_query=" + keywords + "&search_constraint=0&Find=Find&ss=false&ic=16_"+16*(number-1);
			}else{
				for(Element a:page_lis.select("a")){
					//System.out.println(a);
					if(a.text().equals(Integer.toString(number))){
						postfix = a.attr("href");
					}
				}
			}
			String url = "http://www.walmart.com/search/search-ng.do?search_constraint=0&Find=Find&_ta=1&search_query="
					+ keywords + "&" + postfix;
			String jsp = getConnection(url);
			Document redirect_doc = Jsoup.parse(jsp);
			printInConsole(redirect_doc, number);
			return;

		}

	}
	/**
	 * 
	 * @param keywords
	 * 					Search key word
	 * @param number
	 * 					Target page number
	 * @return
	 * @throws IOException
	 */
	public String queryTwo(String keywords, int number) throws IOException{
		/* replace any space between keywords by "%20" */
		int cur = 1;
		keywords = keywords.replace(" ", "%20");
		String url = "http://www.walmart.com/search/search-ng.do?search_constraint=0&Find=Find&_ta=1&search_query="
				+ keywords;
		//System.out.println(url);
		String jsp = getConnection(url);
		Document doc = Jsoup.parse(jsp);
		if(number == cur){
			printInConsole(doc,number);
		}else{
			//redirectPage(doc,keywords,number);
			redirectPageSimple(doc,keywords,number);
		}
		if (jsp == null) {
			System.out.println("Failed to retrieve web page.");
		}
		return jsp;
	}
	
	/**
	 * Test if string is numeric
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}

	
	public static void main(String[] args) throws IOException{
		WalmartRTS rst = new WalmartRTS();
		int num_args = args.length;
		if(num_args==0){
			System.out.println("Please input in following two formats:");
			System.out.println("  java -jar Assignment.jar <keyword>");
			System.out.println("  java -jar Assignment.jar <keyword> <page number>");
		}else if(num_args<0 || num_args>2){
			System.out.println("Wrong input format");
			System.out.println("Please input in following two format");
			System.out.println("java -jar Assignment.jar <keyword>");
			System.out.println("java -jar Assignment.jar <keyword> <page number>");
		}else if(num_args == 1){	
			String total = rst.queryOne(args[0]);
			if(isNumeric(total)){
				System.out.println("Total num of Results: "+total);
			}else{
				System.out.println("No match reuslt");
			}
		}else if(num_args == 2){
			try{
				int number = Integer.parseInt(args[1]);
				rst.queryTwo(args[0],number);
			}catch(NumberFormatException e){
				System.out.println("Wrong input format. Second arg should be Integer");
			}
				
		}
	}


}
