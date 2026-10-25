package miniproject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** 프로그램 시작. 화면은 MainFrame, 데이터 관리는 ExpenseManager가 담당한다. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Path data = Paths.get(System.getProperty("money.data", "data/money.tsv"));
                ExpenseManager manager = new ExpenseManager(data);
                MainFrame.setupTheme();
                new MainFrame(manager, data).setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    "저장 데이터를 읽을 수 없습니다.\n" + ex.getMessage(),
                    "시작 실패", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "프로그램을 시작할 수 없습니다.\n" + ex.getMessage(),
                    "시작 실패", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
