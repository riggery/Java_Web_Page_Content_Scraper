
public class ResultObject {
	private String title;
	private String price;
	ResultObject(String title, String price){
		this.price = price;
		this.title = title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	public void setPrice(String price){
		this.price = price;
	}
	public String getPrice(){
		return price;
	}
	public String getTitle(){
		return title;
	}
	
}
