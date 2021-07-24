import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    private static final String TOKEN = "1844671453:AAFb3XLn8vgitF8OSulc3prDsdEsWxKwg6E";
    private static final String USER_NAME = "Test3000";

    int element;
    boolean start = false;
    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

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
        if(update.getMessage() != null && update.getMessage().hasText()){
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            String[] elements = {" ","Камень","Ножницы","Бумага"};
            try {
                if(!start && text.equals("/start") || (!start && text.equals("/новая игра"))
                        ||(!start && text.equals("Да") ||(!start && text.equals("да")))){
                    element = startCommand(chatId);
                    start = true;
                    System.out.println("Игра началась, у противника: " + elements[element]);
                }
                else if (start && text.equals("/камень")) {
                    stoneCommand(chatId, element, 1);
                    start = false;
                }
                else if (start && text.equals("/ножницы")) {
                    stoneCommand(chatId, element, 2);
                    start = false;
                }
                else if (start && text.equals("/бумага")) {
                    stoneCommand(chatId, element, 3);
                    start = false;
                }
                else {
                    if (!start && !text.isEmpty()) {
                        String txt = "Введите команду /start или /новая игра для начала игры";
                        execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard).setText(getMessage(txt)));
                    } else if (start && !text.isEmpty()) {
                        String txt = "Вы не закончили игру,"
                                +"\nВыберите /камень , /ножницы или /бумага";
                        execute(new SendMessage(chatId,txt).setReplyMarkup(keyboard).setText(getMessage(txt)));
                    }
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    public int startCommand(Long id){
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
            }else {
                execute(new SendMessage(id, "Ваш противник ходит первым, ожидайте..."));
                Thread.sleep(random.nextInt(2000) + 2000);
                element = random.nextInt(2) + 1;
                execute(new SendMessage(id, "").setReplyMarkup(keyboard)
                        .setText(getMessage("Противник сделал свой ход." +
                                "\nТеперь ваш ход!")));
            }
            return element;
        } catch (TelegramApiException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void stoneCommand(Long id, int element, int myElement) {
        //Камень - 1, ножницы - 2, бумага - 3;
        String[] elements = {" ","Камень","Ножницы","Бумага"};

        try {
            if (elements[element].equals(elements[myElement])) {
                execute(new SendMessage(id, "Ничья! Вы оба выбрали " + elements[myElement] + " !!!"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
            }
            if ((myElement == 1 && element == 2)|| (myElement == 2 && element == 3)
                    || (myElement == 3 && element ==1)){
                execute(new SendMessage(id, "Вы выиграли!!!\nВаш противник выбрал "+ elements[element] +" !"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
                //start = false;
            }
            if ((myElement == 2 && element == 1)|| (myElement == 3 && element == 2)
                    || (myElement == 1 && element == 3)){
                execute(new SendMessage(id, "Вы проиграли :( \nВаш противник выбрал "+elements[element]+ "!"));
                execute(new SendMessage(id, "Начать новую игру?").setText(getMessage("Начать новую игру?")));
                //start = false;
            }
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String msg){
        List<KeyboardRow> keys = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        if (msg.equals("Начать новую игру?") || msg.equals("Да")){
            keys.clear();
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/start");
            keyboardFirstRow.add("/новая игра");
            keys.add(keyboardFirstRow);
            keyboard.setKeyboard(keys);
            return msg;
        }
        if(msg.equals("Противник сделал свой ход.\nТеперь ваш ход!")
                || msg.equals("Вы ходите первым.")){
            // || msg.equals("/камень") ||
            //msg.equals("/ножницы") || msg.equals("/бумага") ||
            keys.clear();
            keyboardFirstRow.clear();
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
