package miniproject;
import java.util.Scanner;

import java.util.ArrayList;
// 프로그램 시작
// 메뉴 시작
// 사용자 입력
public class Main {
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		ArrayList <Expense> expenses= new ArrayList<>();
		ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
		
		loop:
		while(true) {
			System.out.println("======MONEY======");
			System.out.println();

			System.out.println("1. 지출 등록");//1명 expense.java
			System.out.println("2. 오늘 지출 조회");//2,3,4 1명
			System.out.println("3. 이번 달 지출 조회");
			System.out.println("4. 카테고리별 조회");
			System.out.println("5. 구독 서비스 관리");//1명
			System.out.println("6. 지출 검색");// 6,7 1명
			System.out.println("7. 질출 목록");
			System.out.println("8. 지출 삭제");//1명
			System.out.println("0. 종료");
			
			System.out.print("선택 >> ");
			int menu = sc.nextInt();
			
		switch(menu) {
		case 0:
			System.out.println("종료");
			break loop;
		case 1:
			System.out.print("====1.지출 등록 ====");
			System.out.println();
			System.out.print("금액 >> ");
			int amount = sc.nextInt();
			sc.nextLine();
			
			System.out.print("카테고리 >> ");
			String category = sc.nextLine();
			
			System.out.print("내용 >> ");
			String content = sc.nextLine();
			
			System.out.print("날짜 >> ");
			String date = sc.nextLine();
			
			Expense e = new Expense(amount,category,content,date);
			expenses.add(e);
			System.out.println("현재 저장된 지출 수: " + expenses.size());
			System.out.println();
			System.out.println("===== 등록 결과 =====");
			System.out.println("금액: " + e.getAmount());
			System.out.println("카테고리: " + e.getCategory());
			System.out.println("내용: " + e.getContent());
			System.out.println("날짜: " + e.getDate());
			System.out.println("===================");
			break;
			
		case 2:
			System.out.println("==== 2.지출 조회 ====");
			
			System.out.println("조회 날짜: ");
			sc.nextLine();
			String searchdate = sc.nextLine();
			
			for(int i = 0; i<expenses.size(); i ++) {
				Expense e1 = expenses.get(i);
				
				if(e1.getDate().equals(searchdate)) {
					System.out.println(e1.getAmount() + "원" + "|"
							+ e1.getCategory() + "|"
							+ e1.getContent() + "|"
							+ e1.getDate()
							);
				}
			}
			break;
		case 3:
			System.out.print("==== 3.이번 달 지출 조회 ====");
			System.out.println();
			System.out.println("조회할 월: ");
			sc.nextLine();
			String searchmonth = sc.nextLine();
			
			for(int i = 0; i <expenses.size(); i++) {
				Expense e2 = expenses.get(i);
				if(e2.getDate().startsWith(searchmonth)) {
					System.out.println(e2.getAmount() + "원" + "|"
				+ e2.getCategory() + "|"
				+ e2.getContent() + "|"
				+ e2.getDate());
				}
			}
			break;
			
		case 4:
			System.out.print("==== 4.카테고리별 조회 ====");
			sc.nextLine();
			System.out.print("조회할 카테고리 >> ");
			String searchcategory = sc.nextLine();
			
			for(int i = 0; i <expenses.size(); i ++) {
				Expense e3 = expenses.get(i);
				if(e3.getCategory().equals(searchcategory)) {
					System.out.println(e3.getAmount() + "원" + "|"
				+ e3.getCategory() + "|"
				+ e3.getContent() + "|"
				+ e3.getDate());
				}
			}
			
			break;
		case 5:
			System.out.println("5.구독 서비스 관리");
			System.out.println();
			System.out.println("1.구독 등록");
			System.out.println("2.구독 목록 조회");
			System.out.println("3.구독 삭제");
			System.out.println("0.뒤로가기");
			System.out.println();
			System.out.print("선택 >>");
			
			int sub = sc.nextInt();
			
			switch(sub){
			case 1: 
				System.out.println("== 구독 등록 ==");
				System.out.println();
				System.out.print("서비스 이름 입력: ");
				sc.nextLine();
				String subname = sc.nextLine();
				System.out.print("월 요금 입력: ");
				int paymonth = sc.nextInt();
				System.out.print("결제일 입력: ");
				int payday = sc.nextInt();
				
				Subscription s = new Subscription(subname, paymonth, payday);
				subscriptions.add(s);
				
				System.out.println("서비스 이름: " + s.getName());
				System.out.println("월 요금: " + s.getPrice());
				System.out.println("결제일: " + s.getPayment());
				
				System.out.print("구독 서비스가 등록되었습니다.");
				System.out.print("현재 구독 수: " +subscriptions.size());
				break;
			case 2:
				System.out.println("== 구독 목록 조회 ==");
				
				for(int i = 0; i<subscriptions.size(); i ++) {
					Subscription s1 = subscriptions.get(i);
					
						System.out.println(s1.getName() +"|"
						+s1.getPrice() +"원" + "|"
						+"매월" + s1.getPayment()+"일");
				
				}
				break;
			case 3:
				System.out.print("삭제할 이름 입력: ");
				sc.nextLine();
				String removename = sc.nextLine();
				
				boolean removed = false;
				
				for(int i = 0; i < subscriptions.size(); i ++) {
					Subscription s2 = subscriptions.get(i);
					
					if(s2.getName().equals(removename)) {
						subscriptions.remove(i);
						removed = true;
						break;
					} 					
				}
				
				if(removed) {
					System.out.println(removename + "삭제 완료");
				} else {
					System.out.println("찾을 수 없음");
				}		
				break;
			case 0:
				
			break;
			}
			
			
		case 6:
			System.out.println("==== 6. 지출 검색 ====");
			System.out.println();
			System.out.println("검색어: ");
			sc.nextLine();
			String searchspend = sc.nextLine();
			
			boolean spend = false;
			for(int i = 0; i < expenses.size(); i ++) {
				Expense e4 = expenses.get(i);
				if(e4.getContent().contains(searchspend)) {
					System.out.println(e4.getAmount() + "원" + "|"
				+ e4.getCategory() + "|"
				+ e4.getContent() + "|"
				+ e4.getDate());
					spend = true;
				} 
			}
			if(spend = false) {
				System.out.println("검색 없음");
			}
			break;
			
		case 7:
			System.out.print("==== 7.통계 ====");
			int total = 0;
			for(int i = 0; i<expenses.size(); i ++) {
				Expense e5 = expenses.get(i);
				total += e5.getAmount();
			System.out.println("총 지출 금액:" + total);
			System.out.println("총 지출 건수: " + expenses.size() + "건");
			}
			if(expenses.size()>0) {
				int avg = total / expenses.size();
				System.out.println("평균 지출 금액: " + avg);
			}
			
			
			break;
		
		default :
			System.out.print("잘못된 번호입니다.");
			
			}
		}
	}
}
