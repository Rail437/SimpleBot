public class user {

    private long ID = 0;
    private int CASH = 0;
    private boolean statusActiv = false;

    public user(long ID) {
        this.ID = ID;
    }


    public long getID() {
        return ID;
    }

    public int getCASH() {
        return CASH;
    }

    public boolean getStatusActiv() {
        return statusActiv;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setCASH(int CASH) {
        this.CASH = CASH;
    }

    public void setStatusActiv(boolean statusActiv) {
        this.statusActiv = statusActiv;
    }
}
