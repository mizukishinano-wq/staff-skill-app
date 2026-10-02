import java.time.LocalDate;

class Staff {
  int id;
  String name;
  String mainPosition;
  LocalDate joinDate;

  Staff(int id, String name, String mainPosition, LocalDate joinDate) {
    this.id = id;
    this.name = name;
    this.mainPosition = mainPosition;
    this.joinDate = joinDate;
  }

  String showStatus() {
    return String.format("%s/メイン:%s/入店日:%s", this.name, this.mainPosition, this.joinDate);
  }

  String toCSV() {
    return String.format("%d,%s,%s,%s", this.id, this.name, this.mainPosition, this.joinDate);
  }
}
