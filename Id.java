package Commands;

public class Id{
    private static long ID=0;

    public static void plusID(){
        ID=ID+1;}


    public static long makeID() {
        Id.plusID();
        return ID;
    }

    public static long getID() {
        return ID;
    }

    public static void setID(long ID) {
        Id.ID = ID;
    }
}