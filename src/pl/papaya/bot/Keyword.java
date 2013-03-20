package pl.papaya.bot;

import pl.spychalski.Utils;

public class Keyword {

	public String Hash;
	public String Url;
	public String Title;
	public String Keywords;
	
	public Keyword(String Url, String Title, String Keywords) {
		
		this.Hash = Utils.MD5(Url);
		this.Url = Url;
		this.Title = Title;
		this.Keywords = Keywords;
		
	}
	
}
