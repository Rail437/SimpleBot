public class User {

    private long ID = 0;
    private int CASH = 0;
    private String name;
    private int stage = 0;
    private int element = 0;
    private int bet = 0;
    private long opponent = 0;
    private boolean first = false;

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

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public void setElement(int element) { this.element = element;}

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

    public void setStage(int stage) { this.stage = stage; }

    public void plusCASH(int i) {
        this.CASH += i;
    }

    public boolean checkName(String txt){
        if(this.name.equals(txt)){
         return true;
        }
        return false;
    }

    public long getOpponent() {
        return opponent;
    }

    public void setOpponent(long opponent) {
        this.opponent = opponent;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}
