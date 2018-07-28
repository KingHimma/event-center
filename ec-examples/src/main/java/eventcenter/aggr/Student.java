package eventcenter.aggr;

public class Student {
    private String name;
     
    private String mobile;
     
    private String score;
     
    private String classroom;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getScore() {
        return score;
    }
    public void setScore(String score) {
        this.score = score;
    }
    public String getClassroom() {
        return classroom;
    }
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    
    @Override
    public String toString() {
    	return new StringBuilder("{name:").append(name).append(",mobile:").append(mobile)
    			.append(",score:").append(score).append(",classroom:").append(classroom).append("}").toString();
    }
}
