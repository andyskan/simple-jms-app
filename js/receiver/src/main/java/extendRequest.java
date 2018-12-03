public class extendRequest {

    public String senderName;
    public int age;
    public String address;

    public int duration;
    public String studentYearEnd;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStudentYearEnd() {
        return studentYearEnd;
    }

    public void setStudentYearEnd(String studentYearEnd) {
        this.studentYearEnd = studentYearEnd;
    }

    @Override
    public String toString() {
        return "extendRequest{" +
                "senderName='" + senderName + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                ", duration=" + duration +
                ", studentYearEnd='" + studentYearEnd + '\'' +
                '}';
    }
}
