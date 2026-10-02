import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class StaffSkillApp {
  static int nextStaffId = 1;
  static int nextTaskId = 1;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    ArrayList<Staff> staffList = new ArrayList<>();
    ArrayList<Task> taskList = new ArrayList<>();
    ArrayList<Skill> skillList = new ArrayList<>();
    while (true) {
      System.out.println("——操作を入力してください。——");
      System.out.print("1/スタッフ登録 2/スタッフ一覧 3/仕事登録 4/仕事一覧 5/スキル登録・変更 6/仕事からできる人を探す 0/終了>");
      int select = inputNumber(sc, 0, 6);
      switch (select) {
        case 1:
          addStaff(staffList, sc);
          break;
        case 2:
          displayStaffList(staffList);
          break;
        case 3:
          addTask(taskList, sc);
          break;
        case 4:
          displayTaskList(taskList);
          break;
        case 5:
          updateSkill(skillList, staffList, taskList, sc);
          break;
        case 6:
          searchStaffByTask(skillList, staffList, taskList, sc);
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
    String mainPosition = selectPosition(sc, "メインポジション");
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

  static void addTask(ArrayList<Task> taskList, Scanner sc) {
    System.out.println("新しい仕事を登録します。");
    System.out.print("仕事名を入力してください>>");
    String name = sc.next();
    String position = selectPosition(sc, "ポジション");
    for (Task t : taskList) {
      if (t.name.equals(name) && t.position.equals(position)) {
        System.out.println(position + "の「" + name + "」はすでに登録されています。");
        return;
      }
    }
    Task t = new Task(nextTaskId, name, position);
    nextTaskId++;
    taskList.add(t);
    System.out.println(position + "の「" + name + "」を登録しました。");
  }

  static void displayTaskList(ArrayList<Task> taskList) {
    if (taskList.size() == 0) {
      System.out.println("仕事はまだ登録されていません。");
      return;
    }
    System.out.println("【仕事一覧】");
    for (String position : Position.NAMES) {
      boolean hasTask = false;
      for (Task t : taskList) {
        if (t.position.equals(position)) {
          if (!hasTask) {
            System.out.println("■" + position);
            hasTask = true;
          }
          System.out.printf("  %d・・・%s%n", t.id, t.name);
        }
      }
    }
  }

  static void updateSkill(ArrayList<Skill> skillList, ArrayList<Staff> staffList, ArrayList<Task> taskList, Scanner sc) {
    if (staffList.size() == 0) {
      System.out.println("先にスタッフを登録してください。");
      return;
    }
    if (taskList.size() == 0) {
      System.out.println("先に仕事を登録してください。");
      return;
    }
    System.out.println("スキルを登録・変更します。");
    Staff staff = selectStaff(staffList, sc);
    Task task = selectTask(taskList, sc);

    Skill skill = findSkill(skillList, staff.id, task.id);
    int currentLevel = 0;
    if (skill != null) {
      currentLevel = skill.level;
    }
    System.out.printf("%sさんの「%s」の今の習熟度:%s%n", staff.name, task.name, Skill.LEVEL_NAMES[currentLevel]);

    System.out.println("新しい習熟度を番号で選んでください。");
    for (int i = 0; i < Skill.LEVEL_NAMES.length; i++) {
      System.out.printf("%d/%s ", i, Skill.LEVEL_NAMES[i]);
    }
    System.out.print(">>");
    int level = inputNumber(sc, 0, Skill.LEVEL_NAMES.length - 1);

    if (level == 0) {
      if (skill != null) {
        skillList.remove(skill);
      }
    } else if (skill == null) {
      skillList.add(new Skill(staff.id, task.id, level, LocalDate.now()));
    } else {
      skill.changeLevel(level);
    }
    System.out.printf("%sさんの「%s」を「%s」にしました。%n", staff.name, task.name, Skill.LEVEL_NAMES[level]);
    displayStaffSkills(staff, skillList, taskList);
  }

  static void displayStaffSkills(Staff staff, ArrayList<Skill> skillList, ArrayList<Task> taskList) {
    System.out.println("【" + staff.name + "さんのスキル】");
    boolean hasSkill = false;
    for (int level = Skill.LEVEL_NAMES.length - 1; level >= 1; level--) {
      for (Skill skill : skillList) {
        if (skill.staffId == staff.id && skill.level == level) {
          Task task = findTask(taskList, skill.taskId);
          System.out.printf("  %s/%s:%s(更新日:%s)%n", task.position, task.name, Skill.LEVEL_NAMES[level], skill.updatedDate);
          hasSkill = true;
        }
      }
    }
    if (!hasSkill) {
      System.out.println("  まだスキルは登録されていません。");
    }
  }

  static void searchStaffByTask(ArrayList<Skill> skillList, ArrayList<Staff> staffList, ArrayList<Task> taskList, Scanner sc) {
    if (taskList.size() == 0) {
      System.out.println("先に仕事を登録してください。");
      return;
    }
    System.out.println("仕事からできる人を探します。");
    Task task = selectTask(taskList, sc);

    System.out.println("どの習熟度以上の人を表示しますか？");
    for (int i = 1; i < Skill.LEVEL_NAMES.length; i++) {
      System.out.printf("%d/%s以上 ", i, Skill.LEVEL_NAMES[i]);
    }
    System.out.print(">>");
    int minLevel = inputNumber(sc, 1, Skill.LEVEL_NAMES.length - 1);

    System.out.printf("【%s/%sが%s以上の人】%n", task.position, task.name, Skill.LEVEL_NAMES[minLevel]);
    boolean found = false;
    for (int level = Skill.LEVEL_NAMES.length - 1; level >= minLevel; level--) {
      for (Skill skill : skillList) {
        if (skill.taskId == task.id && skill.level == level) {
          Staff staff = findStaff(staffList, skill.staffId);
          System.out.printf("  %s(メイン:%s):%s(更新日:%s)%n", staff.name, staff.mainPosition, Skill.LEVEL_NAMES[level], skill.updatedDate);
          found = true;
        }
      }
    }
    if (!found) {
      System.out.println("  この条件に当てはまるスタッフはいません。");
    }
  }

  static Staff selectStaff(ArrayList<Staff> staffList, Scanner sc) {
    displayStaffList(staffList);
    System.out.print("スタッフの番号を入力してください>>");
    while (true) {
      int id = inputNumber(sc, 1, nextStaffId - 1);
      Staff staff = findStaff(staffList, id);
      if (staff != null) {
        return staff;
      }
      System.out.print("その番号のスタッフはいません。もう一度入力してください>>");
    }
  }

  static Task selectTask(ArrayList<Task> taskList, Scanner sc) {
    displayTaskList(taskList);
    System.out.print("仕事の番号を入力してください>>");
    while (true) {
      int id = inputNumber(sc, 1, nextTaskId - 1);
      Task task = findTask(taskList, id);
      if (task != null) {
        return task;
      }
      System.out.print("その番号の仕事はありません。もう一度入力してください>>");
    }
  }

  static Staff findStaff(ArrayList<Staff> staffList, int id) {
    for (Staff s : staffList) {
      if (s.id == id) {
        return s;
      }
    }
    return null;
  }

  static Task findTask(ArrayList<Task> taskList, int id) {
    for (Task t : taskList) {
      if (t.id == id) {
        return t;
      }
    }
    return null;
  }

  static Skill findSkill(ArrayList<Skill> skillList, int staffId, int taskId) {
    for (Skill skill : skillList) {
      if (skill.staffId == staffId && skill.taskId == taskId) {
        return skill;
      }
    }
    return null;
  }

  static String selectPosition(Scanner sc, String label) {
    System.out.println(label + "を番号で選んでください。");
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
