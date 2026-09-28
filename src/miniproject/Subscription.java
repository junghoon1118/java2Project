package miniproject;
// 구독 서비스 데이터
// 이름 / 월 요금 / 결제일 등
public class Subscription {
	private String name;
	private int price;
	private int paymentday;
	
	Subscription(String name, int price, int paymentday){
		this.name = name;
		this.price = price;
		this.paymentday = paymentday;
	}
	
	String getName() {
		return name;
	}
	
	int getPrice() {
		return price;
	}
	
	int getPayment() {
		return paymentday;
	}
}
