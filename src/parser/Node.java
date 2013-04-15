/**
 * 
 */
package parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * Global dynamic class to go through documents and create XML tree.
 * 
 * Compromise between two philosophies :
 * 	- structure with an abstract class implemented by two distincts subclasses, but go through the docs done by methods calls
 *  - or everything proceeded in Constructor (more simple), but need an only Class -> empty data structure with null pointers
 *  
 *  Wich one would be the best?
 *  
 *  Question : why not extend the Element class? (need to wrap it?)
 * 
 * @author Raimbault Juste <br/> <a href="mailto:juste.raimbault@polytechnique.edu">juste.raimbault@polytechnique.edu</a>
 *
 *
 */
public class Node {
	
	/**
	 * Table of parsed path.
	 * 
	 * Used not to do it many times.
	 */
	public static final HashMap<String,int[]> parsedIds= new HashMap<String,int[]>();
	
	/**
	 * Set to store constructed Nodes.
	 * 
	 * HashConsing : a node is constructed one and only one time.
	 * (through the static mathod construct).
	 * 
	 */
	public static final HashMap<Node,Node> nodes = new HashMap<Node,Node>();
	
	public static final HashSet<Node> constructing = new HashSet<Node>();

	
	/**
	 * Function called in the case of a Leaf, to fill the List of Messages.
	 */
	public void extractMessages(){
		//check, in case of a bad use of the function?
		if(isLeaf){
			/*
			 * Extraction heuristic : 
			 * 
			 *  - get all elements of class "master" -> corresponds to message or author
			 *  - find a way to make couples : (message,corresponding author element)
			 *  - extract text and author from all couples, construct corresponding Message.
			 *  
			 */
			
			Elements elements = dom.getElementsByClass("master");
			
			HashMap<Element,Element> couples = new HashMap<Element,Element>();
			
			//dirty way in O(n^2), but not so many messages?
			//SO DIRTY... :(
			for(Element e1:elements){
				for(Element e2:elements){
					if(!e2.equals(e1)){
						if(e1.parent().equals(e2.parent())){
							//put the couple in couples if not already done (2 times)
							if(!couples.containsKey(e1)&&!couples.containsValue(e1))couples.put(e1, e2);
						}
					}
				}
			}
			
			for(Entry<Element,Element> entry:couples.entrySet()){
				
				Element authorEl,textEl;
				String author="";
				String text="";
				
				if(entry.getKey().getElementsByTag("div").isEmpty()){
					authorEl = entry.getKey().getElementsByTag("b").first();
					textEl = entry.getValue().getElementsByTag("div").first();
				}
				else{
					textEl = entry.getKey().getElementsByTag("b").first();
					authorEl = entry.getValue().getElementsByTag("div").first();
				}				
				
				try{
					//deal with links : delete the head of the link
					for(Element link : textEl.getElementsByTag("a")){				
						link.attr("href", link.attr("href").split("/web/")[1].substring(15));
					}
					
					for(Element img : textEl.getElementsByTag("img")){				
						img.attr("src", "web.archive.org"+img.attr("src"));
					}
					
					//extract text with chars unescape
					author = authorEl.html();//StringEscapeUtils.escapeHtml4(authorEl.html());
					text = textEl.html();//StringEscapeUtils.escapeHtml4(textEl.html());
					
					
				}catch(Exception e){}
				
				messages.add(new Message(author,text));

			}
			
			
		}
	}
	
	
	
	
	
	/**
	 * If the Node is a Leaf or a proper node.
	 */
	public boolean isLeaf ;
	
	/**
	 * The unique url designing the node on the web.
	 */
	public URL url;
	
	/**
	 * The String corresponding to the URL.
	 */
	public String path;
	
	public String intpath;
	
	/**
	 * DOM corresponding to the adress.
	 */
	public org.jsoup.nodes.Document dom;
	
	
	/**
	 * The exploded and parsed String.
	 * 
	 * Length of this tab will express the depth level.
	 */
	public int[] ids;
	
	
	/**
	 * Temporary table used to store same level node which are destined to be the same Node.
	 * 
	 * Pb : bad memory optimization?
	 */
	public HashSet<Node> neighbours;
	
	
	/**
	 * Auxiliary field.
	 */
	public String author;
	
	/**
	 * Title of subject.
	 */
	public String title;
	
	/**
	 * html content of message.
	 * 
	 * Field used only when it's a Leaf.<br/>
	 * List in order.
	 */
	public LinkedList<Message> messages;
	
	/**
	 * First level children of the Node.
	 */
	public HashSet<Node> children;
	
	
	public boolean isCorrected=false;
	
	public void correct(){
		/*
		 * first replace the abstracts images of neighbors by the true pointers
		 * (bord effect due to the recursive construction of the nodes)
		 * first called on the root, so no problem it's the true pointer.
		 * get can't fail because n is always in the map if it's in children or neighbours.
		 */
		Log.output("Correcting Node "+this.toString());
		isCorrected = true;
		HashSet<Node> rep = new HashSet<Node>();
		for(Node n:neighbours){ Node ne = nodes.get(n);rep.add(ne);if(!ne.isCorrected)ne.correct();}
		neighbours.clear();
		neighbours.addAll(rep);
		rep.clear();
		for(Node n:children){ Node ne = nodes.get(n);rep.add(ne);if(!ne.isCorrected)ne.correct();}
		children.clear();
		children.addAll(rep);
	}
	
	
	/**
	 * Method used at last to suppress neighbors.
	 * 
	 * Reduce children, then fuse them.<br/>
	 * 
	 * Does nothing on a Leaf.
	 * 
	 */
	public void reduce(){
		
		Log.output("Reducing Node "+this.toString());
		
		if(!isLeaf){
			//reduce all children
			for(Node n:children){
				n.reduce();
			}
			
			//if only one child, won't have neighbours, reducing is not necessary
			if(children.size()>1){
			
				//then fuses the same subjects
				int totalNeighbours = 1;
				Node currentNode = null;
				
				while (totalNeighbours>0){
					//select one child with neighbours
					for(Node c:children){if(c.neighbours.size()>0){currentNode=c;break;}}
					if(currentNode==null)break;
					
					Log.output("In reduce, fusion of node "+currentNode.toString());
					
					//fuses that child with its neighbors
					for(Node neigh:currentNode.neighbours){
						currentNode.fusion(neigh);
						if(children.contains(neigh))children.remove(neigh);
					}
					//clears currentNode neighbours.
					currentNode.neighbours.clear();
					
					//count total neighbours
					totalNeighbours=0;
					for(Node c:children)totalNeighbours+=c.neighbours.size();
				}
			}
		}	
	}
	
	
	/**
	 * Fusion with an other node
	 * 
	 * @requires the other Node has the same depth level
	 * @param n
	 * @return
	 */
	public void fusion(Node n){
		Log.output("Fusing node "+this.toString()+" and node "+n.toString());
		//gather children in this one?
		if(isLeaf){
			//TODO order?? -> maybe an attr give the number of the message, then sorting the list would be the solution
			//if not, would need to sort function of the last number before calling fusion.
			for(Message m:n.messages) messages.addLast(m);
		}
		else{
			for(Node nn:n.children)children.add(nn);
		}
	}
	
	
	public void setLatestDom(){
		try{
			//TODO set timeout as option?
			URLConnection con = new URL(path).openConnection();
			con.setReadTimeout(1000);
			dom = Jsoup.parse(con.getInputStream(),"UTF-8",path);
			
			Parser.totalRequests++;
		}
		catch(java.net.SocketTimeoutException e){Parser.timesOut++;Log.output("TIMEOUT!");dom = new org.jsoup.nodes.Document("<html></html>");}
		catch(org.jsoup.HttpStatusException e){dom = new org.jsoup.nodes.Document("<html></html>");}
		catch(Exception e){dom = new org.jsoup.nodes.Document("<html></html>");Log.output("ERROR:"+e.toString());}
	}
	
	
	
	/**
	 * The main function which will build all the tree.
	 * 
	 * Global method :
	 * 
	 * <ol>
	 * <li>Construct ids from url parsing. -> parsing at this moment. memorize in HashTab, in order not to parse again.</li>
	 * <li>Get all links.</li>
	 * <li>Filter the subjects or page links. (PROVE CONDITION) TODO </li>
	 * <li>Filter only sons and neighbours? (to avoid infinite loop). TODO prove condition.</li>
	 * <li>Construct Corresponding Nodes. (recursive) neighbours in neighbours, sons in children.</li>
	 * <li><s>Fusion of different pages</s>  at this moment?<br/>NO because won't work, Nodes can be pointed by the father too</li>
	 * <li>Give conditions to have a Leaf.</li>
	 * <li>if Leaf -> Extract messages. TODO
	 * if Node -> finished also.</li>
	 * 
	 * </ol>
	 * 
	 * 
	 * @param urlpath
	 */
	public Node(String urlpath,String t){
		intpath = urlpath.substring(20);

		Log.newLine(2);Log.output("Building Node corresponding to internal URL "+intpath);
		
		//init set of neighbours, children and messages
		children = new HashSet<Node>();
		neighbours = new HashSet<Node>();
		messages = new LinkedList<Message>();
		
		//the path is the url
		path = "http://web.archive.org"+urlpath;
		
		title=t;
		
		//creates corresponding url
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {System.out.println(e.toString());}
		
		//parse the path if not already done.
		if(parsedIds.containsKey(path)){
			ids = parsedIds.get(path);
		}
		else{
			//parse the String
			ids = Parser.parse(path);
			parsedIds.put(path, ids);
		}
		
		//get html doc corresponding to the node
		this.setLatestDom();
		
		
		//filter links and create corresponding Nodes
		String currentLink="",currentTitle="";
		for(Element e : dom.getElementsByTag("a")){
			currentLink = e.attr("href");
			currentTitle=e.text();
			int [] numbers;
			if(parsedIds.containsKey(currentLink)){
				numbers = parsedIds.get(currentLink);
			}
			else{
				numbers = Parser.parse(currentLink);
				parsedIds.put(currentLink, numbers);
			}
			
			//fst filter : numbers.length>0 ? is that condition enough? -> YES
			if(numbers.length>0){
				
				/*
				 * 2nd filter : the link corresponds to a node which can be neighbour or child only
				 * Condition : lengths==&&only last number different (-> neighbour)
				 * OR length'=length+1
				 * condition enough too? -> also check if not the same Node.
				 */
				
				//neighbour
				if(ids.length==numbers.length){
					//test if really a neighbour
					boolean isNeighbour = true;
					for(int i=0;i<ids.length - 1;i++){isNeighbour=isNeighbour&&(ids[i]==numbers[i]);}
					//has to be not itself!
					isNeighbour = isNeighbour&&(!Arrays.equals(ids, numbers));
					if(isNeighbour){
						neighbours.add(construct(currentLink,currentTitle));
					}
				}
				//child
				else if(ids.length+1==numbers.length){
					children.add(construct(currentLink,currentTitle));
				}
			}
		}
		
		//theorically, the Node is a Leaf if and only if he has no children
		isLeaf = children.isEmpty();
		
		//in the case of a leaf, extract messages.
		if(isLeaf){
			//find an heuristic to extract messages. -> cf function
			this.extractMessages();
			
			Log.output("This Node was a Leaf, Messages :");
			for(Message m:messages){Log.newLine(1);Log.output(m.toString());};
		}
		
		//TODO extract author, title, other interesting informations. ??
		//for a Leaf : done, for a Node ?? Yes for title.
		
		
		
		//export in xml file to visualize it?
		
		
	}
	
	
	
	
	
	/**
	 * Constructor used for HashConsing.
	 * 
	 * @param s urlpath
	 * @param ids table of ids
	 */
	public Node(String s,int[] i){
		path=s;ids=i;
	}
	
	
	
	
	public static Node construct(String path,String title){
		int[] i = Parser.parse(path);
		if(!parsedIds.containsValue(path)){parsedIds.put(path, i);}
		Node n = new Node(path,i);
		
		if(nodes.containsKey(n)){
			return nodes.get(n);
		}
		else{
			//first put the virtual current node in the map, in order to memorize he's being constructed
			nodes.put(n, n);
			Node res = new Node(path,title);
			nodes.remove(n);
			nodes.put(res, res);
			return res;
		}
	}
	
	
	
	/**
	 * Override of equals to manage HashConsing.
	 * 
	 * <p>
	 * Question : url equals or ids?<br/>
	 * ids would permit to cancel enventuals many dates for the same page..<br/>
	 * -> No pb because doublons will be killed in reduction after.
	 * 
	 * </p>
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o){
		return (o instanceof Node)&&(Arrays.equals((((Node) o).ids),ids));
	}
	
	/**
	 * Idem as equals.
	 */
	@Override
	public int hashCode(){
		int res=0;
		for(int i=0;i<ids.length;i++)res=(10*res)+ids[i];
		return res;
	}
	
	
	/**
	 * Constructor used to debug.
	 * 
	 * @param d
	 * @param leaf
	 */
	public Node(Document d,boolean leaf){
		dom = d;
		isLeaf = leaf;
		messages = new LinkedList<Message>();
	}
	
	
	
	/**
	 * toString Override for debug purposes.
	 */
	@Override
	public String toString(){
		//indentation is the level
		String indent = "";
		for(int i=0;i<ids.length;i++)indent+="   ";
		
		String res = indent+"Node of level "+(ids.length-1)+" and url "+path+"\n";//+indent+"NEIGHBOURS:\n";
		//for(Node n:neighbours)res+= "  "+n.toString()+"\n";
		//res+=indent+"CHILDREN : \n";
		//for(Node c:children)res+=c.toString()+"\n";
		return res;
	}
	
	
	
	
	
	
	
	

}
