import java.time.LocalDate;

class Staff {
  int id;
  String name;
  String mainPosition;
  LocalDate joinDate;
  boolean active;

  Staff(int id, String name, String mainPosition, LocalDate joinDate) {
    this.id = id;
    this.name = name;
    this.mainPosition = mainPosition;
    this.joinDate = joinDate;
    this.active = true;
  }

  boolean isNewcomer() {
    LocalDate limit = this.joinDate.plusMonths(3);
    return !limit.isBefore(LocalDate.now());
  }

  String displayName() {
    if (this.isNewcomer()) {
      return this.name + "(新人)";
    }
    return this.name;
  }

  String showStatus() {
    return String.format("%s/メイン:%s/入店日:%s", this.displayName(), this.mainPosition, this.joinDate);
  }

  String toCSV() {
    String status = "在籍";
    if (!this.active) {
      status = "退職";
    }
    return String.format("%d,%s,%s,%s,%s", this.id, this.name, this.mainPosition, this.joinDate, status);
  }
}
