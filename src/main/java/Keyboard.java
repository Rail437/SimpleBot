import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {
    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

    KeyboardRow keyboardFirstRow = new KeyboardRow();
    KeyboardRow keyboardSecondRow = new KeyboardRow();
    KeyboardRow keyboardThreeRow = new KeyboardRow();

    public Keyboard() {
    }

    public synchronized String getKeyboard(String msg){
        List<KeyboardRow> keys = new ArrayList<>();

        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        if (msg.equals("Начать новую игру?") || msg.equals("Да")
                || msg.equals("/камень")|| msg.equals("/ножницы")|| msg.equals("/бумага")
                || msg.contains("для начала игры")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardSecondRow.add("/мой баланс");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keyboard.setKeyboard(keys);
        }
        if (msg.chars().allMatch(Character :: isDigit) || msg.contains("для начала игры")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardFirstRow.add("/мой баланс");
            keyboardSecondRow.add("10");
            keyboardSecondRow.add("50");
            keyboardSecondRow.add("100");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keyboard.setKeyboard(keys);
        }
        if (msg.contains("нет") || msg.equals("Нет")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardFirstRow.add("/мой баланс");
            keys.add(keyboardFirstRow);
            keyboard.setKeyboard(keys);
        }
        if (msg.contains("режим") || msg.equals("выберите")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("/против бота");
            keyboardSecondRow.add("/против соперника");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keyboard.setKeyboard(keys);
        }
        if (msg.contains("приглашает") || msg.contains("пришлите в ответ")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardFirstRow.add("Да");
            keyboardFirstRow.add("Нет");
            keys.add(keyboardFirstRow);
            keyboard.setKeyboard(keys);
        }
        if(msg.contains("еперь ваш ход!")
                || msg.equals("Противник сделал свой ход.\nТеперь ваш ход!")
                || msg.contains("ходите первым")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardThreeRow.clear();
            keyboardFirstRow.add("/камень");
            keyboardFirstRow.add("/ножницы");
            keyboardSecondRow.add("/бумага");
            keyboardSecondRow.add("/новая игра");
            keyboardThreeRow.add("/баланс");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keys.add(keyboardThreeRow);
            keyboard.setKeyboard(keys);
        }
        if (msg.contains("аланс") || msg.contains("счете")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardThreeRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardFirstRow.add("/Баланс");
            keyboardSecondRow.add("/Пополнить");
            keyboardSecondRow.add("/Вывести");
            keyboardThreeRow.add("100");
            keyboardThreeRow.add("/Отмена");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keys.add(keyboardThreeRow);
            keyboard.setKeyboard(keys);
        }
        return msg;
    }

    public synchronized String getKeyboardCash(String msg){
        List<KeyboardRow> keys = new ArrayList<>();

        keyboard.setSelective(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        if (msg.contains("аланс") || msg.contains("счете")){
            keyboardFirstRow.clear();
            keyboardSecondRow.clear();
            keyboardThreeRow.clear();
            keyboardFirstRow.add("/новая игра");
            keyboardFirstRow.add("/Баланс");
            keyboardSecondRow.add("/Пополнить");
            keyboardSecondRow.add("/Вывести");
            keyboardThreeRow.add("100");
            keyboardThreeRow.add("/Отмена");
            keys.add(keyboardFirstRow);
            keys.add(keyboardSecondRow);
            keys.add(keyboardThreeRow);
            keyboard.setKeyboard(keys);
        }
        return msg;
    }
}
