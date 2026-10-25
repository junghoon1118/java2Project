package miniproject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** 데이터 검증, 검색, 파일 저장을 담당한다. */
public class ExpenseManager {
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final Path file;
    public ExpenseManager(Path file) throws IOException {
        this.file = file.toAbsolutePath();
        if (!Files.exists(this.file)) return;
        int line = 0;
        try {
            for (String row : Files.readAllLines(this.file, StandardCharsets.UTF_8)) {
                line++;
                String[] p = row.split("\t", -1);
                if (p[0].equals("E") && p.length == 5) {
                    int a = Integer.parseInt(p[1]); String c = decode(p[2]), t = decode(p[3]);
                    validateExpense(a,c,t,p[4]); expenses.add(new Expense(a,c,t,p[4]));
                } else if (p[0].equals("S") && p.length == 4) {
                    String n = decode(p[1]); int a = Integer.parseInt(p[2]), d = Integer.parseInt(p[3]);
                    validateSubscription(n,a,d); subscriptions.add(new Subscription(n,a,d));
                } else throw new IllegalArgumentException();
            }
        } catch (RuntimeException ex) { throw new IOException("저장 파일 " + line + "번째 줄에 오류가 있습니다. 원본 파일을 확인하세요.",ex); }
    }
    static void required(String s) { if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("빈 항목을 입력해 주세요."); }
    static void validateExpense(int a,String c,String t,String d) {
        if(a<=0) throw new IllegalArgumentException("금액은 1원 이상이어야 합니다.");
        required(c); required(t); LocalDate.parse(d);
    }
    static void validateSubscription(String n,int a,int d) {
        required(n); if(a<=0) throw new IllegalArgumentException("월 요금은 1원 이상이어야 합니다.");
        if(d<1||d>31) throw new IllegalArgumentException("결제일은 1~31일입니다.");
    }
    public List<Expense> expenses() { return new ArrayList<>(expenses); }
    public List<Subscription> subscriptions() { return new ArrayList<>(subscriptions); }
    public List<Expense> search(String date,String category,String keyword) {
        List<Expense> found = new ArrayList<>();
        for(Expense e:expenses) if(e.getDate().startsWith(date) && (category.isEmpty()||e.getCategory().equalsIgnoreCase(category)) && e.getContent().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) found.add(e);
        found.sort(Comparator.comparing(Expense::getDate).reversed()); return found;
    }
    public void putExpense(Expense old,int amount,String category,String content,String date) throws IOException {
        validateExpense(amount,category,content,date);
        List<Expense> next=expenses(); Expense e=new Expense(amount,category.trim(),content.trim(),date);
        if(old==null) next.add(e); else next.set(next.indexOf(old),e);
        commit(next,subscriptions);
    }
    public void removeExpense(Expense e) throws IOException { List<Expense> next=expenses(); next.remove(e); commit(next,subscriptions); }
    public void putSubscription(Subscription old,String name,int price,int day) throws IOException {
        validateSubscription(name,price,day); name=name.trim();
        for(Subscription s:subscriptions) if(s!=old&&s.getName().equalsIgnoreCase(name)) throw new IllegalArgumentException("이미 등록된 구독입니다.");
        List<Subscription> next=subscriptions(); Subscription s=new Subscription(name,price,day);
        if(old==null) next.add(s); else next.set(next.indexOf(old),s); commit(expenses,next);
    }
    public void removeSubscription(Subscription s) throws IOException { List<Subscription> next=subscriptions(); next.remove(s); commit(expenses,next); }
    public long subscriptionTotal() { return subscriptions.stream().mapToLong(Subscription::getPrice).sum(); }
    public static LocalDate nextPayment(Subscription s,LocalDate today) {
        YearMonth m=YearMonth.from(today); LocalDate next=m.atDay(Math.min(s.getPayment(),m.lengthOfMonth()));
        if(next.isBefore(today)) { m=m.plusMonths(1); next=m.atDay(Math.min(s.getPayment(),m.lengthOfMonth())); } return next;
    }
    private static String encode(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    private static String decode(String s) { return new String(Base64.getDecoder().decode(s),StandardCharsets.UTF_8); }
    private void commit(List<Expense> es,List<Subscription> ss) throws IOException {
        List<String> lines=new ArrayList<>();
        for(Expense e:es) lines.add("E\t"+e.getAmount()+"\t"+encode(e.getCategory())+"\t"+encode(e.getContent())+"\t"+e.getDate());
        for(Subscription s:ss) lines.add("S\t"+encode(s.getName())+"\t"+s.getPrice()+"\t"+s.getPayment());
        Files.createDirectories(file.getParent()); Path temp=Files.createTempFile(file.getParent(),"money-",".tmp");
        try {
            Files.write(temp,lines,StandardCharsets.UTF_8);
            try { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException ex) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
        List<Expense> ec=new ArrayList<>(es); List<Subscription> sc=new ArrayList<>(ss);
        expenses.clear(); expenses.addAll(ec); subscriptions.clear(); subscriptions.addAll(sc);
    }
}
