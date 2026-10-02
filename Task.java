class Task {
  int id;
  String name;
  String position;

  Task(int id, String name, String position) {
    this.id = id;
    this.name = name;
    this.position = position;
  }

  String toCSV() {
    return String.format("%d,%s,%s", this.id, this.name, this.position);
  }
}
