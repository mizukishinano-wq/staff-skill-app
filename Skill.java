import java.time.LocalDate;

class Skill {
  static final String[] LEVEL_NAMES = {"未経験", "教わった", "1人でできる", "教えられる"};

  int staffId;
  int taskId;
  int level;
  LocalDate updatedDate;

  Skill(int staffId, int taskId, int level, LocalDate updatedDate) {
    this.staffId = staffId;
    this.taskId = taskId;
    this.level = level;
    this.updatedDate = updatedDate;
  }

  void changeLevel(int level) {
    this.level = level;
    this.updatedDate = LocalDate.now();
  }

  String toCSV() {
    return String.format("%d,%d,%d,%s", this.staffId, this.taskId, this.level, this.updatedDate);
  }
}
