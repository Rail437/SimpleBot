public class Commands {

    private String[] commands = {"амен","ожниц","умаг","нет","Нет","sers"};

    private String[] selectCommands ={"ригласить друга", "играть с другом"};

    public Commands() {
    }

    public boolean isContainsStoneCommand(String txt){
        for (int i = 0; i <commands.length ; i++) {
            if(txt.contains(commands[i])){
                return true;
            }
        }
        return false;
    }
    public boolean isContainsSelectCommand(String txt){
        for (int i = 0; i <selectCommands.length ; i++) {
            if(txt.contains(selectCommands[i])){
                return true;
            }
        }
        return false;
    }


    public String[] getCommands() {
        return commands;
    }

    public void setCommands(String[] commands) {
        this.commands = commands;
    }
}
