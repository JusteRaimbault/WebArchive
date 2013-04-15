/**
 * 
 */
package parser;

import java.io.File;
import java.io.FileWriter;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * @author Raimbault Juste <br/> <a href="mailto:juste.raimbault@polytechnique.edu">juste.raimbault@polytechnique.edu</a>
 *
 */
public class XMLExporter {
	
	/**
	 * Converts the Node to a jdom Document.
	 * 
	 * @requires the Node has been prealably reduced
	 * 
	 * @param n Node to convert
	 * @return the corresponding Document
	 */
	public static Document convertDoc(Node n){
		Document res = new Document();
		Element root = convertNode(n,"forum");
		res.setRootElement(root);
		return res;
	}
	
	public static Element convertNode(Node n,String name){
		Element res = new Element(name);
		res.setAttribute("title", n.title);
		res.setAttribute("url",n.path);
		if(!n.isLeaf){
			for(Node c:n.children){
				Element convertedChild = convertNode(c,"subject");
				res.getChildren().add(convertedChild);
			}
		}
		else{
			if(n.messages.size()==0)res.setAttribute("error","true"); else res.setAttribute("error","false");
			for(Message m:n.messages){
				Element message = new Element("message");
				message.setText(m.text);
				message.getAttributes().add(new Attribute("author",m.author));
				res.getChildren().add(message);
			}
		}
		return res;
	}
	
	/**
	 * Write a jdom doc in file.
	 * 
	 * @param doc
	 * @param filename
	 */
	public static void export(Document doc,String filename){
		try{
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("         \n").setEncoding("ISO-8859-1"));
			outputter.output(doc, new FileWriter(new File(filename)));
		}catch(Exception e){e.printStackTrace();}
	}
	

}
