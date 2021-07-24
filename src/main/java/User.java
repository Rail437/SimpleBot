public class User {

    private long ID = 0;
    private int CASH = 0;
    private String name;
    private int stage = 0;
    private int element = 0;

    private boolean statusActiv = false;

    public User(long ID, String name) {

        this.ID = ID;
        this.name = name;
    }

    public long getID() {
        return ID;
    }

    public String getName() { return name; }

    public int getCASH() {
        return CASH;
    }

    public boolean getStatusActiv() {
        return statusActiv;
    }

    public int getStage() { return stage; }

    public int getElement() { return element; }

    public void setElement(int element) { this.element = element;}

    public void setName(String name) { this.name = name; }

    public void setID(long ID) {
        this.ID = ID;
    }

    public boolean minusCASH(int i){
        int buff = this.CASH;
        if((this.CASH - i) < 0){
            this.CASH = buff;
            return false;
        }else {
            this.CASH -= i;
        }
        return true;
    }



    public void setStatusActiv(boolean statusActiv) {
        this.statusActiv = statusActiv;
    }

    public void setStage(int stage) { this.stage = stage; }

    public void plusCASH(int i) {
        this.CASH += i;
    }
}
