import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class StaffSkillApp {
  static final File STAFF_FILE = new File("staffs.csv");
  static final File TASK_FILE = new File("tasks.csv");
  static final File SKILL_FILE = new File("skills.csv");

  static int nextStaffId = 1;
  static int nextTaskId = 1;

  public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    ArrayList<Staff> staffList = loadStaffFile(STAFF_FILE);
    ArrayList<Task> taskList = loadTaskFile(TASK_FILE);
    ArrayList<Skill> skillList = loadSkillFile(SKILL_FILE);
    System.out.printf("スタッフ%d人、仕事%d件、スキル%d件を読み込みました。%n", staffList.size(), taskList.size(), skillList.size());
    while (true) {
      System.out.println("——操作を入力してください。——");
      System.out.print("1/スタッフ登録 2/スタッフ一覧 3/仕事登録 4/仕事一覧 5/スキル登録・変更 6/仕事からできる人を探す 7/スキルマップ 0/終了>");
      int select = inputNumber(sc, 0, 7);
      switch (select) {
        case 1:
          addStaff(staffList, sc);
          saveAll(staffList, taskList, skillList);
          break;
        case 2:
          displayStaffList(staffList);
          break;
        case 3:
          addTask(taskList, sc);
          saveAll(staffList, taskList, skillList);
          break;
        case 4:
          displayTaskList(taskList);
          break;
        case 5:
          updateSkill(skillList, staffList, taskList, sc);
          saveAll(staffList, taskList, skillList);
          break;
        case 6:
          searchStaffByTask(skillList, staffList, taskList, sc);
          break;
        case 7:
          displaySkillMap(skillList, staffList, taskList, sc);
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
    String name = inputText(sc);
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
    String name = inputText(sc);
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

  static void displaySkillMap(ArrayList<Skill> skillList, ArrayList<Staff> staffList, ArrayList<Task> taskList, Scanner sc) {
    if (staffList.size() == 0) {
      System.out.println("先にスタッフを登録してください。");
      return;
    }
    if (taskList.size() == 0) {
      System.out.println("先に仕事を登録してください。");
      return;
    }
    System.out.println("表示するポジションを番号で選んでください。");
    System.out.print("0/全て ");
    for (int i = 0; i < Position.NAMES.length; i++) {
      System.out.printf("%d/%s ", i + 1, Position.NAMES[i]);
    }
    System.out.print(">>");
    int no = inputNumber(sc, 0, Position.NAMES.length);

    ArrayList<Task> mapTasks = new ArrayList<>();
    for (String position : Position.NAMES) {
      if (no != 0 && !position.equals(Position.NAMES[no - 1])) {
        continue;
      }
      for (Task t : taskList) {
        if (t.position.equals(position)) {
          mapTasks.add(t);
        }
      }
    }
    if (mapTasks.size() == 0) {
      System.out.println("このポジションの仕事はまだ登録されていません。");
      return;
    }

    int nameWidth = displayWidth("名前");
    for (Staff s : staffList) {
      nameWidth = Math.max(nameWidth, displayWidth(s.name));
    }

    String title = "全て";
    if (no != 0) {
      title = Position.NAMES[no - 1];
    }
    System.out.println("【スキルマップ:" + title + "】");
    System.out.print(padRight("名前", nameWidth) + "  ");
    for (Task t : mapTasks) {
      System.out.print(padRight(t.name, displayWidth(t.name)) + "  ");
    }
    System.out.println();
    for (Staff s : staffList) {
      System.out.print(padRight(s.name, nameWidth) + "  ");
      for (Task t : mapTasks) {
        Skill skill = findSkill(skillList, s.id, t.id);
        String cell = "-";
        if (skill != null) {
          cell = String.valueOf(skill.level);
        }
        System.out.print(padRight(cell, displayWidth(t.name)) + "  ");
      }
      System.out.println();
    }
    System.out.println("(3:教えられる 2:1人でできる 1:教わった -:未経験)");
  }

  static int displayWidth(String text) {
    int width = 0;
    for (char c : text.toCharArray()) {
      if (c < 0x80 || (c >= 0xFF61 && c <= 0xFF9F)) {
        width += 1;
      } else {
        width += 2;
      }
    }
    return width;
  }

  static String padRight(String text, int width) {
    return text + " ".repeat(width - displayWidth(text));
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

  static String inputText(Scanner sc) {
    while (true) {
      String text = sc.next();
      if (!text.contains(",")) {
        return text;
      }
      System.out.print("「,」は使えません。もう一度入力してください>>");
    }
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

  static ArrayList<Staff> loadStaffFile(File file) throws Exception {
    ArrayList<Staff> list = new ArrayList<>();
    if (!file.exists()) {
      return list;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    String line;
    while ((line = br.readLine()) != null) {
      String[] values = line.split(",");
      int id = Integer.parseInt(values[0]);
      String name = values[1];
      String mainPosition = values[2];
      LocalDate joinDate = LocalDate.parse(values[3]);
      list.add(new Staff(id, name, mainPosition, joinDate));
      if (id >= nextStaffId) {
        nextStaffId = id + 1;
      }
    }
    br.close();
    return list;
  }

  static ArrayList<Task> loadTaskFile(File file) throws Exception {
    ArrayList<Task> list = new ArrayList<>();
    if (!file.exists()) {
      return list;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    String line;
    while ((line = br.readLine()) != null) {
      String[] values = line.split(",");
      int id = Integer.parseInt(values[0]);
      String name = values[1];
      String position = values[2];
      list.add(new Task(id, name, position));
      if (id >= nextTaskId) {
        nextTaskId = id + 1;
      }
    }
    br.close();
    return list;
  }

  static ArrayList<Skill> loadSkillFile(File file) throws Exception {
    ArrayList<Skill> list = new ArrayList<>();
    if (!file.exists()) {
      return list;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    String line;
    while ((line = br.readLine()) != null) {
      String[] values = line.split(",");
      int staffId = Integer.parseInt(values[0]);
      int taskId = Integer.parseInt(values[1]);
      int level = Integer.parseInt(values[2]);
      LocalDate updatedDate = LocalDate.parse(values[3]);
      list.add(new Skill(staffId, taskId, level, updatedDate));
    }
    br.close();
    return list;
  }

  static void saveAll(ArrayList<Staff> staffList, ArrayList<Task> taskList, ArrayList<Skill> skillList) throws Exception {
    ArrayList<String> staffLines = new ArrayList<>();
    for (Staff s : staffList) {
      staffLines.add(s.toCSV());
    }
    saveFile(STAFF_FILE, staffLines);

    ArrayList<String> taskLines = new ArrayList<>();
    for (Task t : taskList) {
      taskLines.add(t.toCSV());
    }
    saveFile(TASK_FILE, taskLines);

    ArrayList<String> skillLines = new ArrayList<>();
    for (Skill skill : skillList) {
      skillLines.add(skill.toCSV());
    }
    saveFile(SKILL_FILE, skillLines);
  }

  static void saveFile(File file, ArrayList<String> lines) throws Exception {
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
    for (String line : lines) {
      bw.write(line);
      bw.newLine();
    }
    bw.close();
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
