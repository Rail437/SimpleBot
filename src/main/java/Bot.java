import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;
public class Bot extends TelegramLongPollingBot {
    private static final String TOKEN = "-";
    private static final String USER_NAME = "-";

    int element;
    boolean start = false;
    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
    Long chatId;
    Map<Long, User> users = new HashMap<>();

    public Bot(DefaultBotOptions options){
        super(options);
    }

    @Override
    public String getBotUsername() {
        return USER_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;
        chatId = update.getMessage().getChatId();
        sendUser(chatId, update);
    }

    private void sendUser (Long chatId, Update update){
        if(users.get(chatId) == null) {
            users.put(chatId, new User(chatId,update.getMessage().getFrom().getFirstName()));
            System.out.println("Chat number: " + chatId);
            System.out.println("name: " + update.getMessage().getFrom().getFirstName());
        }

        String text = update.getMessage().getText();
        System.out.println(update.getMessage().getFrom().getFirstName() + " text: "+ text);
        String[] elements = {" ", "Камень", "Ножницы", "Бумага"};

        try {
            if ( users.get(chatId).getStage() == 0 && (text.equals("/start") || (text.contains("овая игра"))
                    || (text.equals("Да") || (!start && text.equals("да"))))) {
                startCommand(chatId);
                element = users.get(chatId).getElement();
                System.out.println("Игра началась, против "+users.get(chatId).getName()+ " противник выбрал: " + elements[element]);
            } else if (users.get(chatId).getStage() == 1 && text.contains("амен")) {
                stoneCommand(chatId, 1);
            } else if (users.get(chatId).getStage() == 1 && text.contains("ножниц")) {
                stoneCommand(chatId, 2);
            } else if (users.get(chatId).getStage() == 1 && text.contains("бумаг")) {
                stoneCommand(chatId, 3);
            } else if (users.get(chatId).getStage() == 0 && text.contains("аланс")) {
                checkMyCash(chatId);
            } else {
                if (users.get(chatId).getStage() == 0 && !text.isEmpty() ) {
                    String txt = "Введите команду /start или /новая игра для начала игры";
                    execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard).setText(getMessage(txt)));
                } else if (!text.isEmpty() && users.get(chatId).getStage() == 1) {
                    String txt = "Вы не закончили игру,"
                            + "\nВыберите /камень , /ножницы или /бумага";
                    execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard).setText(getMessage(txt)));

                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void checkMyCash(long chatId) {

    }

    public void startCommand(Long id){
        int element;
        Random random = new Random();
        try {
            execute(new SendMessage(id, "Начнем игру! \nИщем противника..."));
            Thread.sleep(random.nextInt(2000) + 2000);
            execute(new SendMessage(id, "Противник найден"));
            if(random.nextBoolean()){
                execute(new SendMessage(id, "Вы ходите первым.")
                .setReplyMarkup(keyboard).setText(getMessage("Вы ходите первым.")));
                element = random.nextInt(2) + 1;
                users.get(chatId).setElement(element);
            }else {
                execute(new SendMessage(id, "Ваш противник ходит первым, ожидайте..."));
                Thread.sleep(random.nextInt(2000) + 2000);
                element = random.nextInt(2) + 1;
                execute(new SendMessage(id, "").setReplyMarkup(keyboard)
                        .setText(getMessage("Противник сделал свой ход." +
                                "\nТеперь ваш ход!")));
                users.get(chatId).setElement(element);
            }
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
        users.get(chatId).setStage(1);
    }

    public void stoneCommand(Long id, int myElement) {
        //Камень - 1, ножницы - 2, бумага - 3;
        String[] elements = {" ","Камень","Ножницы","Бумага"};
        element = users.get(chatId).getElement();
        try {
            if (elements[element].equals(elements[myElement])) {
                execute(new SendMessage(id, "Ничья! Вы оба выбрали " + elements[myElement] + " !!!"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
            }
            if ((myElement == 1 && element == 2)|| (myElement == 2 && element == 3)
                    || (myElement == 3 && element ==1)){
                execute(new SendMessage(id, "Вы выиграли!!!\nВаш противник выбрал "+ elements[element] +" !"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
            }
            if ((myElement == 2 && element == 1)|| (myElement == 3 && element == 2)
                    || (myElement == 1 && element == 3)){
                execute(new SendMessage(id, "Вы проиграли :( \nВаш противник выбрал "+elements[element]+ "!"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
            }
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
        users.get(chatId).setStage(0);
    }

    public String getMessage(String msg){
        List<KeyboardRow> keys = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        if (msg.equals("Начать новую игру?") || msg.equals("Да")
                || msg.equals("/камень")|| msg.equals("/ножницы")|| msg.equals("/бумага")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/start");
            keyboardFirstRow.add("/новая игра");
            keyboardSecondRow.add("/мой баланс");
            keys.add(keyboardFirstRow);
            keyboard.setKeyboard(keys);
            return msg;
        }
        if (msg.contains("нет") || msg.equals("Нет")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardFirstRow.add("/мой баланс");
            keys.add(keyboardFirstRow);
            keyboard.setKeyboard(keys);
            return msg;
        }
        if(msg.equals("Противник сделал свой ход.\nТеперь ваш ход!")
                || msg.equals("Вы ходите первым.")){
            // || msg.equals("/камень") ||
            //msg.equals("/ножницы") || msg.equals("/бумага") ||
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/камень");
            keyboardFirstRow.add("/ножницы");
            keyboardSecondRow.add("/бумага");
            keyboardSecondRow.add("/новая игра");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keyboard.setKeyboard(keys);
        }
        return msg;
    }
}
