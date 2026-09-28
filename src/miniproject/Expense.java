package miniproject;
// 지출 한 건의 데이터
// 날짜 / 금액 / 카테고리 / 내용
public class Expense {
	private int amount;
	private String category;
	private String content;
	private String date;
	
	Expense (int amount, String category, String content, String date){
		this.amount = amount;
		this.category = category;
		this.content = content;
		this.date = date;
	}
	
	int getAmount() {
		return amount;
	}
	
	String getCategory() {
		return category;
	}
	
	String getContent() {
		return content;
	}
	
	String getDate() {
		return date;
	}
}

