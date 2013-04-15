/**
 * 
 */
package parser;

/**
 * Class representing a message.
 * 
 * @author Raimbault Juste <br/> <a href="mailto:juste.raimbault@polytechnique.edu">juste.raimbault@polytechnique.edu</a>
 *
 */
public class Message {
	
	public Message(String a,String t){text = t;author=a;}
	
	public String text;
	
	public String author;
	
	
	@Override
	public String toString(){
		return "Message by "+author+"\nCONTENT:\n"+text;
	}
	

}
