import java.util.Date;

public class Stock {
    /**
	 * Here: all the needed class members and their getters and setters
	 */
	private String name = "";
	private String ticker = "";
	private Date startDate = null;
	private Integer stockBrokers;
	private String description = "";
	private String exchangeCode = "";
	
	public String getName() {return this.name;}
	public String getTicker() {return this.ticker;}
	public Date getStartDate() {return this.startDate;}
	public Integer getStockBrokers() {return this.stockBrokers;}
	public String getDescription() {return this.description;}
	public String getExchangeCode() {return this.exchangeCode;}
	public void setName(String name) {this.name = name;}
	public void setTicker(String ticker) {this.ticker = ticker;}
	public void setStartDate(Date date) {this.startDate = date;}
	public void setStockBrokers(Integer stockBrokers) {this.stockBrokers = stockBrokers;}
	public void setDescription(String description) {this.description = description;}
	public void setExchangeCode(String exchangeCode) {this.exchangeCode = exchangeCode;}
	
	
	
    public Stock() {
    	

    }
    
    public String toString() {
    	String returnString = getTicker();
    	return returnString;
    }



}

