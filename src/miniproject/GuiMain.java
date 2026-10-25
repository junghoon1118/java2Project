package miniproject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GuiMain {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                // 화면에서 사용하는 데이터 저장 위치
                Path data = Paths.get("data", "money.tsv");

                // 데이터 관리 객체 생성
                ExpenseManager manager =
                        new ExpenseManager(data);

                // 화면 디자인 설정
                MainFrame.setupTheme();

                // 화면 생성 및 표시
                MainFrame frame =
                        new MainFrame(manager, data);

                frame.setVisible(true);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "저장 데이터를 읽을 수 없습니다.\n"
                                + e.getMessage(),
                        "파일 오류",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "프로그램 실행 중 오류가 발생했습니다.\n"
                                + e.getMessage(),
                        "실행 오류",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
