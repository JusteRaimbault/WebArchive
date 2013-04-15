
package parser;

import java.util.Date;

/**
 * Main method which main will organise the proceedings.
 * 
 * @author Raimbault Juste <br/> <a href="mailto:juste.raimbault@polytechnique.edu">juste.raimbault@polytechnique.edu</a>
 *
 */
public class Parser {
	
	public static int totalRequests=0;
	
	public static int timesOut=0;

	public static int[] parse(String s){
		//System.out.println("Parsing string "+s);
		if(!s.contains("http://forum.aceboard.net/")) return new int[0];
		String[] fstsplit = s.split("http://forum.aceboard.net/")[1].split("29408");
		if(fstsplit.length<=1) return new int[0];
		String[] stab =  fstsplit[1].split("-");
		boolean[] isNumber = new boolean[stab.length];
		int n=0;
		for(int i=0;i<stab.length;i++){
			try{Integer.parseInt(stab[i]);isNumber[i]=true;n++;}
			catch(java.lang.NumberFormatException e){isNumber[i]=false;}
		}
		int[] res = new int[n];
		int j=0;
		for(int i=0;i<stab.length;i++){if(isNumber[i]){res[j]=Integer.parseInt(stab[i]);j++;}}
		
		//particluar artifact : no nodes of level 3 or more in theory
		//(comes from number in title - in general should add condition to take only consecutive numbers)
		if (res.length>=4) {int[] r = new int[3];for(int i=0;i<3;i++)r[i]=res[i];}
			
		//case of the root -> put standard format ([0], in order to represent the "pages")
		if(s.endsWith("http://forum.aceboard.net/i-29408.htm")) {res = new int[1];res[0]=0;};
		return res;
	}
	
	
	/**
	 * The parsing of everything in itself.
	 * 
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		//init log file
		Log.initLog();
		
		//build the abstract tree
		long t = System.currentTimeMillis();
		Node root = Node.construct("/web/20120126060400/http://forum.aceboard.net/i-29408.htm","all");
		
		Log.output("All forum parsed in "+(System.currentTimeMillis()-t)+" ms...");
		
		Log.output("timeout % : "+((100*(double)timesOut)/(double)totalRequests));
		
		//corrects then reduces it
		root.correct();
		
		try{Log.output(root.toString());}catch(Exception e){Log.output(e.toString());};
		
		root.reduce();
		
		//export in file
		XMLExporter.export(XMLExporter.convertDoc(root), "res/"+(new Date()).toGMTString().replace(' ', '_').replace('/', ':')+"WebArchive.xml");
		
	}

}
