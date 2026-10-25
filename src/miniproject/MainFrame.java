
package miniproject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.*;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** 화면은 입력과 출력만 담당하고 데이터 변경은 관리 클래스에 위임한다. */
public class MainFrame extends JFrame {
    private final ExpenseManager manager;
    private final JTabbedPane tabs = new JTabbedPane();
    private final Color navy = new Color(24, 36, 60), mint = new Color(19, 132, 112);
    private final JTextField date=new JTextField(10), category=new JTextField(6), keyword=new JTextField(8), month=new JTextField(YearMonth.now().toString(),8);
    private final JLabel summary=new JLabel(), count=new JLabel();
    private final JTextArea statistics=new JTextArea();
    private final DefaultTableModel em=model("날짜","카테고리","내용","금액(원)"), sm=model("서비스","월 요금(원)","결제일","다음 결제일");
    private final JTable et=new JTable(em), st=new JTable(sm);
    private List<Expense> visible;
    private List<Subscription> subs;
    private interface Action { void run() throws Exception; }
    private static DefaultTableModel model(String... names) { return new DefaultTableModel(names,0) { public boolean isCellEditable(int r,int c){return false;} }; }
    /** 화면의 글꼴과 색상을 설정한다. */
    public static void setupTheme() throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof Font)
                UIManager.put(key, new javax.swing.plaf.FontUIResource("Malgun Gothic", Font.PLAIN, 13));
        }
        UIManager.put("Panel.background", new Color(245,247,251));
        UIManager.put("Table.selectionBackground", new Color(214,242,235));
        UIManager.put("Table.selectionForeground", new Color(24,36,60));
    }
    public MainFrame(ExpenseManager manager,Path data) {
        super("MONEY | 지출 · 구독 관리"); this.manager=manager;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() { public void windowClosing(java.awt.event.WindowEvent e) { exit(); } });
        setSize(1260,780); setMinimumSize(new Dimension(1200,700)); setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout(12,12)); root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18)); setContentPane(root);
        JPanel header=new JPanel(new GridLayout(2,1,0,10)); JLabel title=new JLabel("MONEY   지출 · 구독 관리"); title.setFont(title.getFont().deriveFont(Font.BOLD,25f)); header.add(title); header.add(summary); root.add(header,BorderLayout.NORTH);
        tabs.addTab("지출 내역",expensesPanel()); tabs.addTab("구독 관리",subscriptionsPanel()); tabs.addTab("월별 통계",statsPanel()); root.add(tabs); root.add(navigation(), BorderLayout.WEST);
        root.add(new JLabel("변경 시 자동 저장 | "+data.toAbsolutePath()),BorderLayout.SOUTH);
        et.setRowHeight(36); st.setRowHeight(36);
        for (JTable table : new JTable[]{et,st}) {
            table.setShowVerticalLines(false); table.setGridColor(new Color(232,236,242));
            table.getTableHeader().setFont(new Font("Malgun Gothic",Font.BOLD,13));
            table.getTableHeader().setPreferredSize(new Dimension(0,40));
            table.setFillsViewportHeight(true); table.setBackground(Color.WHITE);
        } et.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); st.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); refresh();
    }
    private JButton button(String text,Action action) { JButton b=new JButton(text); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBackground(Color.WHITE); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,223,232)), BorderFactory.createEmptyBorder(9,12,9,12))); b.addActionListener(e->{try{action.run();}catch(java.io.IOException ex){error(ex);}catch(IllegalArgumentException ex){error(ex);}catch(Exception ex){error(ex);}}); return b; }
    private JPanel navigation() {
        JPanel side = new JPanel(new BorderLayout()); side.setBackground(navy);
        side.setBorder(BorderFactory.createEmptyBorder(22,14,22,14)); side.setPreferredSize(new Dimension(205,0));
        JLabel brand = new JLabel("MONEY NOTE"); brand.setForeground(Color.WHITE); brand.setFont(new Font("Malgun Gothic",Font.BOLD,19)); side.add(brand,BorderLayout.NORTH);
        JPanel links = new JPanel(new GridLayout(11,1,0,9)); links.setOpaque(false); links.setBorder(BorderFactory.createEmptyBorder(25,0,0,0));
        links.add(nav("+  지출 등록", () -> { tabs.setSelectedIndex(0); editExpense(false); }));
        links.add(nav("오늘 지출 조회", () -> showExpenses(LocalDate.now().toString())));
        links.add(nav("이번 달 지출 조회", () -> showExpenses(YearMonth.now().toString())));
        links.add(nav("카테고리별 조회", () -> {
            String value=JOptionPane.showInputDialog(this,"조회할 카테고리를 입력하세요.","카테고리별 조회",JOptionPane.QUESTION_MESSAGE);
            if(value!=null) { ExpenseManager.required(value); showExpenses(""); category.setText(value.trim()); refresh(); }
        }));
        links.add(nav("구독 서비스 관리", () -> tabs.setSelectedIndex(1)));
        links.add(nav("지출 검색", () -> {
            String value=JOptionPane.showInputDialog(this,"내용에 포함된 검색어를 입력하세요.","지출 검색",JOptionPane.QUESTION_MESSAGE);
            if(value!=null) { ExpenseManager.required(value); showExpenses(""); keyword.setText(value.trim()); refresh(); }
        }));
        links.add(nav("지출 목록", () -> showExpenses("")));
        links.add(nav("지출 삭제", () -> {
            tabs.setSelectedIndex(0);
            if(et.getSelectedRow()<0) { JOptionPane.showMessageDialog(this,"목록에서 삭제할 지출을 선택한 뒤 다시 눌러주세요."); return; }
            Expense item=selectedExpense(); if(confirm()){manager.removeExpense(item);refresh();}
        }));
        links.add(nav("월별 통계", () -> {tabs.setSelectedIndex(2);refreshStats();}));
        links.add(nav("종료",this::exit));
        JLabel tip=new JLabel("매일 기록하는 작은 습관");tip.setForeground(new Color(180,197,217));links.add(tip);
        side.add(links); return side;
    }
    private JButton nav(String text, Action action) {
        JButton b=button(text,action); b.setHorizontalAlignment(SwingConstants.LEFT); b.setBackground(new Color(40,56,82));b.setForeground(Color.WHITE);return b;
    }
    private void showExpenses(String prefix) {date.setText(prefix);category.setText("");keyword.setText("");tabs.setSelectedIndex(0);refresh();}
    private void exit() {if(JOptionPane.showConfirmDialog(this,"프로그램을 종료할까요? 변경 내용은 자동 저장되어 있습니다.","종료",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)dispose();}
    private JPanel expensesPanel() {
        JPanel p=new JPanel(new BorderLayout(8,8)), filters=new JPanel(new FlowLayout(FlowLayout.LEFT)), actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("날짜/월")); filters.add(date); date.setToolTipText("전체: 빈칸 / YYYY-MM / YYYY-MM-DD"); filters.add(new JLabel("카테고리")); filters.add(category); filters.add(new JLabel("내용")); filters.add(keyword);
        filters.add(button("조회",this::refresh)); 
        filters.add(button("전체",()->{date.setText("");category.setText("");keyword.setText("");refresh();}));
        actions.add(button("+ 지출 등록",()->editExpense(false))); actions.add(button("선택 수정",()->editExpense(true))); actions.add(button("선택 삭제",()->{Expense e=selectedExpense();if(confirm()){manager.removeExpense(e);refresh();}})); actions.add(count);
        p.add(filters,BorderLayout.NORTH); p.add(tableScroll(et));p.add(actions,BorderLayout.SOUTH);return p;
    }
    private JPanel subscriptionsPanel() {
        JPanel p=new JPanel(new BorderLayout()), actions=new JPanel(new FlowLayout(FlowLayout.LEFT));p.add(tableScroll(st));
        actions.add(button("+ 구독 등록",()->editSubscription(false)));actions.add(button("선택 수정",()->editSubscription(true)));actions.add(button("선택 삭제",()->{Subscription s=selectedSubscription();if(confirm()){manager.removeSubscription(s);refresh();}}));
        actions.add(new JLabel("구독료는 예상 고정비로 표시하며 실제 지출과 별도로 관리합니다."));p.add(actions,BorderLayout.SOUTH);return p;
    }
    private JScrollPane tableScroll(JTable table) { JScrollPane pane=new JScrollPane(table); pane.setColumnHeaderView(table.getTableHeader()); return pane; }
    private JPanel statsPanel() {
        JPanel p=new JPanel(new BorderLayout()), controls=new JPanel(new FlowLayout(FlowLayout.LEFT));controls.add(new JLabel("조회 월 (YYYY-MM)")); controls.add(month);controls.add(button("통계 조회",this::refreshStats));p.add(controls,BorderLayout.NORTH);
        statistics.setEditable(false);statistics.setFont(new Font("Malgun Gothic",Font.PLAIN,16));statistics.setMargin(new Insets(20,20,20,20));p.add(new JScrollPane(statistics));return p;
    }
    // <T>는 지출과 구독 등 어떤 자료형의 목록에도 같은 선택 로직을 적용한다.
    private <T> T selected(JTable table, List<T> items, String label) {
        int row = table.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException(label + "을(를) 먼저 선택하세요.");
        return items.get(table.convertRowIndexToModel(row));
    }
    private Expense selectedExpense() { return selected(et, visible, "지출"); }
    private Subscription selectedSubscription() { return selected(st, subs, "구독"); }
    private JPanel form(String[] labels,JTextField... fields) {JPanel p=new JPanel(new GridLayout(labels.length,2,12,12));for(int i=0;i<labels.length;i++){p.add(new JLabel(labels[i]));fields[i].setColumns(18);p.add(fields[i]);}return p;}
    private int number(JTextField f) {return Integer.parseInt(f.getText().trim().replace(",",""));}
    private void editExpense(boolean edit) {
        Expense old=edit?selectedExpense():null;
        JTextField d=new JTextField(old==null?LocalDate.now().toString():old.getDate()), a=new JTextField(old==null?"":""+old.getAmount()), c=new JTextField(old==null?"식비":old.getCategory()), t=new JTextField(old==null?"":old.getContent());
        JPanel form=form(new String[]{"날짜 (YYYY-MM-DD)","금액 (원)","카테고리","내용"},d,a,c,t);
        while(JOptionPane.showConfirmDialog(this,form,edit?"지출 수정":"지출 등록",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try{String ds=d.getText().trim();if(ds.length()!=10)throw new IllegalArgumentException("날짜는 YYYY-MM-DD로 입력하세요.");manager.putExpense(old,number(a),c.getText(),t.getText(),ds);refresh();return;}catch(java.io.IOException ex){error(ex);}catch(IllegalArgumentException ex){error(ex);}catch(Exception ex){error(ex);}
        }
    }
    private void editSubscription(boolean edit) {
        Subscription old=edit?selectedSubscription():null;
        JTextField n=new JTextField(old==null?"":old.getName()), a=new JTextField(old==null?"":""+old.getPrice()), d=new JTextField(old==null?"1":""+old.getPayment());
        JPanel form=form(new String[]{"서비스 이름","월 요금 (원)","결제일 (1~31)"},n,a,d);
        while(JOptionPane.showConfirmDialog(this,form,edit?"구독 수정":"구독 등록",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try{manager.putSubscription(old,n.getText(),number(a),number(d));refresh();return;}catch(java.io.IOException ex){error(ex);}catch(IllegalArgumentException ex){error(ex);}catch(Exception ex){error(ex);}
        }
    }
    private boolean confirm(){return JOptionPane.showConfirmDialog(this,"선택한 항목을 삭제할까요?","삭제 확인",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;}
    private String won(long n){return String.format("%,d원",n);}
    private void refresh(){
        String f=date.getText().trim();if(!f.isEmpty()){if(f.length()==7)YearMonth.parse(f);else{LocalDate.parse(f);if(f.length()!=10)throw new IllegalArgumentException("날짜 형식을 확인하세요.");}}
        visible=manager.search(f,category.getText().trim(),keyword.getText().trim());em.setRowCount(0);
        for(Expense e:visible)em.addRow(new Object[]{e.getDate(),e.getCategory(),e.getContent(),String.format("%,d",e.getAmount())});
        count.setText("조회 "+visible.size()+"건 | 합계 "+won(visible.stream().mapToLong(Expense::getAmount).sum()));
        subs=manager.subscriptions();sm.setRowCount(0);for(Subscription s:subs)sm.addRow(new Object[]{s.getName(),String.format("%,d",s.getPrice()),"매월 "+s.getPayment()+"일",ExpenseManager.nextPayment(s,LocalDate.now())});
        long total=manager.search(YearMonth.now().toString(),"","").stream().mapToLong(Expense::getAmount).sum();summary.setText("이번 달 지출  "+won(total)+"        월 구독료  "+won(manager.subscriptionTotal())+"        등록 구독  "+subs.size()+"개");
        try{refreshStats();}catch(Exception ex){statistics.setText("조회 월을 YYYY-MM 형식으로 입력하세요.");}
    }
    private void refreshStats(){
        YearMonth m=YearMonth.parse(month.getText().trim());List<Expense> rows=manager.search(m.toString(),"","");long total=rows.stream().mapToLong(Expense::getAmount).sum();
        StringBuilder text=new StringBuilder(m+" 지출 통계\n\n총 지출: "+won(total)+"\n지출 건수: "+rows.size()+"건\n건당 평균: "+won(rows.isEmpty()?0:total/rows.size())+"\n\n카테고리별 지출\n");
        Map<String,Long> totals=new TreeMap<>();for(Expense e:rows)totals.merge(e.getCategory(),(long)e.getAmount(),Long::sum);
        if(rows.isEmpty())text.append("등록된 지출이 없습니다.\n");
        for(Map.Entry<String,Long> e:totals.entrySet())text.append(e.getKey()).append(" : ").append(won(e.getValue())).append(String.format(" (%.1f%%)\n",100.0*e.getValue()/total));
        text.append("\n현재 월 구독료: ").append(won(manager.subscriptionTotal())).append("\n연간 구독 예상액: ").append(won(manager.subscriptionTotal()*12)).append("\n\n※ 구독 예상액은 실제 지출 합계에 자동 합산되지 않습니다.\n※ 29~31일 결제는 짧은 달에 말일로 표시합니다.");statistics.setText(text.toString());statistics.setCaretPosition(0);
    }
    private void error(Exception ex){String msg=ex instanceof NumberFormatException?"금액과 결제일은 정수 범위 내 숫자로 입력하세요.":ex instanceof java.time.format.DateTimeParseException?"날짜 형식을 확인하세요. 예: 2026-10-25 / 월: 2026-10":ex.getMessage();JOptionPane.showMessageDialog(this,msg,"입력 / 저장 확인",JOptionPane.WARNING_MESSAGE);}
}
