import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
public class Bot extends TelegramLongPollingBot {
    private static final String TOKEN = "-";
    private static final String USER_NAME = "-";

    int element;
    boolean start = false;
    Keyboard keyboard = new Keyboard();
    Long chatId;
    Map<Long, User> users = new HashMap<>();
    String[] elements = {" ", "Камень", "Ножницы", "Бумага"};

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

    /**
     * Статус 0 - не начал игру, 1 - не выбрал камень или ножницы.
      * Статус 2 - ожидаем сумму для ввода
     * Статус 3 - ожидаем сумму для вывода
     */
    private void sendUser (Long chatId, Update update){
        if(users.get(chatId) == null) {
            users.put(chatId, new User(chatId,update.getMessage().getFrom().getFirstName()));
            System.out.println("Chat number: " + chatId);
            System.out.println("name: " + update.getMessage().getFrom().getFirstName());
        }

        String text = update.getMessage().getText();
        System.out.println(update.getMessage().getFrom().getFirstName() + " text: " + text);
        try {
        if (users.get(chatId).getStage() == 0 && (text.equals("/start") || (text.contains("овая игра"))
                || (text.equals("Да") || (!start && text.equals("да"))))) {
            startCommand(chatId);
            element = users.get(chatId).getElement();
            System.out.println("Игра началась, против " + users.get(chatId).getName() + " противник выбрал: " + elements[element]);
        } else if (users.get(chatId).getStage() == 1 && text.contains("амен")) {
            stoneCommand(chatId, 1);
        } else if (users.get(chatId).getStage() == 1 && text.contains("ножниц")) {
            stoneCommand(chatId, 2);
        } else if (users.get(chatId).getStage() == 1 && text.contains("бумаг")) {
            stoneCommand(chatId, 3);
        } else if (users.get(chatId).getStage() == 0 && text.contains("аланс")) {
            checkMyCash(chatId);
        } else if (users.get(chatId).getStage() == 0 && text.contains("sers")) {
            checkUsers();
        } else if ((users.get(chatId).getStage() == 2 || users.get(chatId).getStage() == 3)
                && text.contains("тмена")) {
            users.get(chatId).setStage(2);
            execute( new SendMessage(chatId, "Введите").setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash("Для начала игры введите /новая игра," +
                            "\nдля уточнения баланса введите /баланс")));
        } else if (users.get(chatId).getStage() == 0 && text.contains("ополнит")) {
            execute( new SendMessage(chatId, "Введите").setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash("Введите цифрами какую сумму хотите внести")));
            users.get(chatId).setStage(2);
        } else if (users.get(chatId).getStage() == 0 && text.contains("ывести")) {
            execute( new SendMessage(chatId, "Введите").setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash("Введите цифрами какую сумму хотите вывести")));
            users.get(chatId).setStage(3);
        } else if (users.get(chatId).getStage() == 2 && checkNumber(text)) {
            cashPlus(chatId, Integer.valueOf(text));
        } else if (users.get(chatId).getStage() == 3 && checkNumber(text)) {
            cashMinus(chatId, Integer.valueOf(text));
        } else {
            if (users.get(chatId).getStage() == 0 && !text.isEmpty()) {
                String txt = "Введите команду /start или /новая игра для начала игры";
                execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
            } else if (!text.isEmpty() && users.get(chatId).getStage() == 1) {
                String txt = "Вы не закончили игру,"
                        + "\nВыберите /камень , /ножницы или /бумага";
                execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
            } else if (users.get(chatId).getStage() == 2 && !checkNumber(text)) {
                String txt = "Введите сумму цифрами без лишних символов или напишите /отмена для выхода из режима пополнения";
                execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
            }
        }
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удалить потом список поименно.
     */
    private void checkUsers() throws TelegramApiException {
        List<String> usersList = new ArrayList<>();
        for (Map.Entry<Long, User> entry : users.entrySet()) {
             usersList.add(entry.getValue().getName());
        }

        String txt = "Сейчас в боте " + users.size() + " участников онлайн. " +
                "\n" + usersList;
        execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboardCash(txt)));
    }

    private boolean checkNumber(String text) {
        return text.chars().allMatch( Character::isDigit );
    }

    private void cashMinus(Long chatId, Integer sum) throws TelegramApiException{
        if (users.get(chatId).minusCASH(sum)) {
            execute(new SendMessage(chatId, "Вывод").setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash("Вы вывели со счета" + sum + " рублей.")));
            String txt = "На вашем счете осталось: " + users.get(chatId).getCASH() +
                    " рублей.";
            users.get(chatId).setStage(0);
            execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboardCash(txt)));
        } else {
            execute(new SendMessage(chatId, "Нет денег").setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash("На Вашем счете не достаточно средств для вывода.")));
            users.get(chatId).setStage(3);
        }
    }

    private void cashPlus(Long chatId, Integer sum) throws TelegramApiException {
        System.out.println("Пришло после valueOf: " + sum);
        users.get(chatId).plusCASH(sum);
        execute( new SendMessage(chatId, "Пополнение").setReplyMarkup(keyboard.keyboard)
                .setText(keyboard.getKeyboardCash("Вы пополнили счет на "+ sum +" рублей.")));
        String txt = "На вашем счете : " + users.get(chatId).getCASH() + " рублей.";
        users.get(chatId).setStage(0);
        execute( new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboardCash(txt)));
    }

    private void checkMyCash(long chatId) throws TelegramApiException {
        String txt = "На вашем счете: " + users.get(chatId).getCASH() +
                " рублей.";
        execute(new SendMessage(chatId, txt).setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboardCash(txt)));
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
                .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard("Вы ходите первым.")));
                element = random.nextInt(2) + 1;
            }else {
                execute(new SendMessage(id, "Ваш противник ходит первым, ожидайте..."));
                Thread.sleep(random.nextInt(2000) + 2000);
                element = random.nextInt(2) + 1;
                execute(new SendMessage(id, "").setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboard("Противник сделал свой ход." +
                                "\nТеперь ваш ход!")));
            }
            users.get(chatId).setElement(element);
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
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((myElement == 1 && element == 2)|| (myElement == 2 && element == 3)
                    || (myElement == 3 && element ==1)){
                execute(new SendMessage(id, "Вы выиграли!!!\nВаш противник выбрал "+ elements[element] +" !"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((myElement == 2 && element == 1)|| (myElement == 3 && element == 2)
                    || (myElement == 1 && element == 3)){
                execute(new SendMessage(id, "Вы проиграли :( \nВаш противник выбрал "+elements[element]+ "!"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
        users.get(chatId).setStage(0);
    }
}
