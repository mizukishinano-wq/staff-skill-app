import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class StaffSkillApp {
  static int nextStaffId = 1;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    ArrayList<Staff> staffList = new ArrayList<>();
    while (true) {
      System.out.println("——操作を入力してください。——");
      System.out.print("1/スタッフ登録 2/スタッフ一覧 0/終了>");
      int select = inputNumber(sc, 0, 2);
      switch (select) {
        case 1:
          addStaff(staffList, sc);
          break;
        case 2:
          displayStaffList(staffList);
          break;
        case 0:
          System.out.println("アプリケーションを終了します。");
          return;
      }
    }
  }

  static void addStaff(ArrayList<Staff> staffList, Scanner sc) {
    System.out.println("新しいスタッフを登録します。");
    System.out.print("名前を入力してください>>");
    String name = sc.next();
    String mainPosition = selectPosition(sc);
    LocalDate joinDate = inputDate(sc);
    Staff s = new Staff(nextStaffId, name, mainPosition, joinDate);
    nextStaffId++;
    staffList.add(s);
    System.out.println(name + "さんを登録しました。");
  }

  static void displayStaffList(ArrayList<Staff> staffList) {
    if (staffList.size() == 0) {
      System.out.println("スタッフはまだ登録されていません。");
      return;
    }
    System.out.println("【スタッフ一覧】");
    for (Staff s : staffList) {
      System.out.printf("%d・・・%s%n", s.id, s.showStatus());
    }
  }

  static String selectPosition(Scanner sc) {
    System.out.println("メインポジションを番号で選んでください。");
    for (int i = 0; i < Position.NAMES.length; i++) {
      System.out.printf("%d/%s ", i + 1, Position.NAMES[i]);
    }
    System.out.print(">>");
    int no = inputNumber(sc, 1, Position.NAMES.length);
    return Position.NAMES[no - 1];
  }

  static LocalDate inputDate(Scanner sc) {
    System.out.print("入店日を入力してください(例:2026-04-01)>>");
    while (true) {
      String text = sc.next();
      try {
        return LocalDate.parse(text);
      } catch (DateTimeParseException e) {
        System.out.print("日付の形式が正しくありません。2026-04-01の形で入力してください>>");
      }
    }
  }

  static int inputNumber(Scanner sc, int min, int max) {
    while (true) {
      if (sc.hasNextInt()) {
        int no = sc.nextInt();
        if (no >= min && no <= max) {
          return no;
        }
      } else {
        sc.next();
      }
      System.out.printf("%d~%dの数字を入力してください>", min, max);
    }
  }
}
