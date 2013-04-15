import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import org.jsoup.*;
import org.jsoup.nodes.Element;

import parser.*;

import org.apache.commons.lang3.*;

/**
 * @author Raimbault Juste <br/> <a href="mailto:juste.raimbault@polytechnique.edu">juste.raimbault@polytechnique.edu</a>
 *
 */
@SuppressWarnings("unused")
public class URLRequest {

	
	/**
	 * Prints the source of the given url.
	 * 
	 * @param urlstring path of the url
	 */
	public static void printFromURL(String urlstring){
		try{
			URL url = new URL(urlstring);
	        BufferedReader in = new BufferedReader(
	        new InputStreamReader(url.openStream()));
	
	        String inputLine;
	        while ((inputLine = in.readLine()) != null)
	            System.out.println(inputLine);
	        in.close();
		}
		catch(Exception e){System.out.println("ERROR:"+e.toString());}
	}
	
	
	/**
	 * Build a document from an URL.
	 * 
	 * @param urlpath
	 * @return Document (jsoup context)
	 */
	public static org.jsoup.nodes.Document getDOMFromURL(String urlpath){
		try{
			org.jsoup.nodes.Document document = Jsoup.parse(new URL(urlpath), 1000);
			
			return document;
		}
		catch(java.net.SocketTimeoutException e){return new org.jsoup.nodes.Document("<html></html>");}
		catch(org.jsoup.HttpStatusException e){return new org.jsoup.nodes.Document("<html></html>");}
		catch(Exception e){System.out.println("ERROR:"+e.toString());return null;}
		/*
		try{
			SAXBuilder sax = new SAXBuilder();
			URL url = new URL(urlpath);
			//URLConnection con = url.openConnection();
			//System.out.println("Connection encoding:"+con.getContentEncoding());
			return  sax.build(url);
			
		}
		catch(Exception e){System.out.println("ERROR:"+e.toString());return null;}
		*/
	}
	
	
	
	public static void exploreLinks(Element doc){
		for(Element e : doc.getElementsByTag("a")){
			String s = e.attr("href");
			if(s.contains("http://forum.aceboard.net")){
				s = s.split("http://forum.aceboard.net/")[1];
				if(s.startsWith("29408")){
					System.out.println(s);
					exploreLinks(getDOMFromURL(e.attr("href")));
				}
			}
		}
	}
	
	
	public static void testLater(){
		try{
			String url = "http://web.archive.org/web/20120126060400/http://forum.aceboard.net/i-29408.htm";
			//org.jsoup.nodes.Document document = Jsoup.parse(new URL("http://web.archive.org/web/20120126060400/http://forum.aceboard.net/i-29408.htm"),1000);//http://web.archive.org/web/*/http://forum.aceboard.net/29408-11-10118-2-.htm"), 1000);
			URLConnection con = new URL(url).openConnection();
			con.setReadTimeout(1000);
			org.jsoup.nodes.Document document = Jsoup.parse(con.getInputStream(),"ISO-8859-1",url);

			
			System.out.println(document.outputSettings().charset());
			System.out.println(document.toString());
			Element max=null;
			long m=0;
			for(Element e:document.getElementsByAttributeValue("title","1 snapshots")){
				if(Long.parseLong(e.attr("href").split("/")[2])>m)max=e;
			}
			System.out.println(max.attr("href"));
		}
		catch(Exception e){System.out.println("ERROR:"+e.toString());}
		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String path = "http://web.archive.org/web/20090531164617/http://forum.aceboard.net/29408-11-10633-0-France-UNSS-2009.htm";	
		URLConnection con = new URL(path).openConnection();
		con.setReadTimeout(1000);
		org.jsoup.nodes.Document dom = Jsoup.parse(con.getInputStream(),"UTF-8",path);
		System.out.println(dom);
		
		
		//System.out.println(StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeHtml4("éazerrtyàè%ù<div></div>")));
		//System.out.println(StringEscapeUtils.unescapeHtml4("éazerrtyàè%ù <div></div>"));
		
		
		/*HashMap<Integer,Integer> f = new HashMap<Integer,Integer>();
		Integer n = new Integer(1);
		Integer res = new Integer(2);
		f.put(n, n);
		Integer a = f.get(n);
		n=res;
		//f.remove(n);
		f.put(res, res);
		System.out.println(a);
		System.out.println(f.get(n));
		*/
		
		//testLater();
		
		//tests for basic stream
		//printFromURL("http://9gag.com/");
		//printFromURL("http://www.republiquedesmangues.fr/");
		
		//test to get DOM
		//printFromURL("http://web.archive.org/web/20090417154745/http://forum.aceboard.net/29408-11-2-news.htm");
		//org.jsoup.nodes.Document doc = getDOMFromURL("http://web.archive.org/web/20090621182610/http://forum.aceboard.net/i-29408.htm");
		//for(Element e : doc.children()) System.out.println(e.toString());
		
		//test for going through doc
		
		//exploreLinks(doc);
		/*for(String s : "http://web.archive.org/web/20090621182610/http://forum.aceboard.net/i-29408.htm".split("http://forum.aceboard.net/")[1].split("29408")[1].split("-")){
			
		}*/
		
		///for(int i : Parser.parse("http://web.archive.org/web/20090621182610/http://forum.aceboard.net/i-29408.htm"))System.out.println(i);
		
		/*org.jsoup.nodes.Document document = Jsoup.parse(new File("/Users/Juste/Desktop/test.html"),"ISO-8859-1");
		Node n = new Node(document,true);
		n.extractMessages();
		for(Message m:n.messages)System.out.println(m);
		XMLExporter.export(XMLExporter.convertDoc(n), "test.xml");*/
		
	}

}
