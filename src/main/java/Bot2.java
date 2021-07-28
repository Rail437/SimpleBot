import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot2 extends TelegramLongPollingBot {
    private static final String TOKEN = "1844671453:AAHPAx3sW6WqXUz9sD6X2207uEeu4iyK6XM";
    private static final String USER_NAME = "Test3000";

    Keyboard keyboard = new Keyboard();
    Long chatId;
    Map<Long, User> users = new HashMap<>();

    public Bot2(DefaultBotOptions options){
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

    /**
     * Статус 0 - не начал игру
     * Статус 1 - не выбрал камень или ножницы.
     * Статус 2 - ожидаем сумму для ввода
     * Статус 3 - ожидаем сумму для вывода
     * Статус 4 - ожидание ставки
     * Статус 50 - игра с противником
     * Статус 51 - выбрал противника не сделал ставку / выбирает противника
     * Статус 52 - сделал ставку и отправил запрос противнику
     * Статус 53 - противник согласен, ожидаем ввода Камень, Ножницы или Бумага.
     * Статус 533 - ожидание пока противник введет Камень, Ножницы или Бумага.
     * Статус 55 - ожидание ответа от противника.
     * Статус 56 - сдела ход первым и ждет противника.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || update.getMessage().getText().isEmpty())
            return;
        chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        System.out.println("Chat number: " + chatId + "name: " + update.getMessage().getFrom().getFirstName() + " " + text);
        if(users.get(chatId) == null) {
            users.put(chatId, new User(chatId,update.getMessage().getFrom().getFirstName()));
        }
        User user = users.get(chatId);
        boolean stoneCommand = text.contains("амен") || text.contains("ожниц") || text.contains("умаг");

        try {
            /** Команды выбора режима игры. */
            if (user.getStage() == 0 && text.contains("овая игр")){
                execute(new SendMessage(chatId, "Пока так")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboard("Выберите режим игры.")));
            }
            /** Игра против бота */
            else if (user.getStage() == 0 && text.contains("ротив бота")){
                startCommand(chatId);
            }
            else if (user.getStage() == 1 && stoneCommand){
                if(text.contains("амен"))
                    stoneCommand(chatId, 1);
                if(text.contains("ожниц"))
                    stoneCommand(chatId, 2);
                if(text.contains("умаг"))
                    stoneCommand(chatId, 3);
            }
            else if (!text.isEmpty() && user.getStage() == 1 && !stoneCommand){
                String txt = "Вы не закончили игру."
                        + "\nВыберите /камень , /ножницы или /бумага";
                execute(new SendMessage(chatId, txt)
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboard(txt)));
            }
            /**
             * Команды просмотра,вывода и пополнения баланса.*/
            else if ((user.getStage() == 0 || user.getStage() == 4) && text.contains("аланс")) { //Баланс
                String txt = "На вашем счете: " + users.get(chatId).getCASH() +
                        " рублей.";
                execute(new SendMessage(chatId, txt)
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash(txt)));
            } else if ((user.getStage() >= 2) && text.contains("тмена")) { //Отмена
                user.setStage(0);
                execute(new SendMessage(chatId, "Введите")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Для начала игры введите /новая игра,\nдля уточнения баланса введите /баланс")));
            } else if ((user.getStage() == 0 || user.getStage() == 4) && text.contains("ополнит")) { //Пополнить
                execute(new SendMessage(chatId, "Введите")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Введите цифрами какую сумму хотите внести")));
                user.setStage(2);
            } else if (user.getStage() == 0 && text.contains("ывести")) { //Вывести
                execute(new SendMessage(chatId, "Введите")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Введите цифрами какую сумму хотите вывести")));
                user.setStage(3);
            } else if (user.getStage() == 2 && text.chars().allMatch(Character::isDigit)) { //номер
                cashPlus(chatId, Integer.valueOf(text));
            } else if (user.getStage() == 3 && text.chars().allMatch(Character::isDigit)) { //номер
                cashMinus(chatId, Integer.valueOf(text));
            }/* else if (user.getStage() == 0){
                execute(new SendMessage(chatId, "Пока так")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Я пока не умею обрабатывать эту команду ((\nДля выбора игры напишите /новая игра\n" +
                                "Для просмотра баланса введите /баланс.")));
            }*/
            /**
             *  Игра против реального человека
             */
            else if (user.getStage() == 0 && text.contains("соперник")){
                checkUsers();
            } else if (user.getStage() == 50 && !update.getMessage().getText().isEmpty()){
                starter(chatId, text);
            }
            if (user.getStage() == 53 && stoneCommand) {
                if (users.get(user.getOpponent()).getStage() == 533){
                    stepOne(chatId, text);
                } else{
                    stepTwo(chatId, text);
                }
            } else if (user.getStage() == 53){
                String txt = "Ваш противник ждет, пока вы сделаете свой выбор. ";
                execute(new SendMessage(user.getOpponent(), txt)
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash(txt)));
            } else if (user.getStage() == 533) {
                String txt = "Пожалуйста подождите, ваш соперник еще не сделал свой выбор.";
                execute(new SendMessage(user.getOpponent(), txt)
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash(txt)));
            }
            if (user.getStage() == 55 && (text.equals("Да")|| text.equals("да"))){
                startGamePVP(chatId);
            }
            if (user.getStage() == 55 && (text.equals("Нет")|| text.equals("нет"))){
                user.setStage(0);
                execute(new SendMessage(user.getOpponent(), "Нет")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Ваш противник отказался от игры. Вы можете выбрать нового соперника или начать игру против бота.")));
            }
            boolean b = text.equals("Да")|| text.equals("да") || text.equals("Нет")|| text.equals("нет");
            if (user.getStage() == 55 && !b){
                execute(new SendMessage(chatId, "Нет")
                        .setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboardCash("Отвас ждут ответа Да или Нет на игру с "
                                + users.get(user.getOpponent()).getName()
                                + " камень, ножницы, бумага.")));
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    /**
     * Старт игры против соперника
     */
    private void starter(Long id, String txt) throws TelegramApiException {
        Long second = checkMap(txt);
        if(second != null){
            startPVP(id,second);
        }
        if(second == null){
            execute(new SendMessage(id, "Не получилось присоединиться к сопернику.\n" +
                    "Введите другое имя из списка тех кто онлайн."));
            checkUsers();
        }

    }

    private void startPVP(Long id, Long secondID){
        users.get(id).setStage(52);
        users.get(id).setOpponent(secondID);
        users.get(secondID).setStage(55);
        users.get(secondID).setOpponent(id);

        try {
            execute(new SendMessage(id, "Отправляем противнику приглашение! \nПосле того как он согласится, начнем!"));
            String txt = "Вас приглашает сыграть " + users.get(id).getName() + ", Вы согласны?" +
                    "\nЕсли да, то пришлите в ответ слово Да.";
            execute(new SendMessage(secondID, txt).setText(keyboard.getKeyboard(txt)));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startGamePVP(Long id) throws TelegramApiException {
        Random random = new Random();
        if(random.nextBoolean()){
            users.get(id).setStage(53);
            User opponent = users.get(users.get(id).getOpponent());
            opponent.setStage(533);
            String txt = "Вы ходите первым.Выбирайте";
            execute(new SendMessage(id, txt)
                    .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
            String txt2 = "Ваш соперник ходит первым. Ожидайте...";
            execute(new SendMessage(opponent.getID(), txt2).setText(keyboard.getKeyboard(txt2)));
        }else {
            users.get(id).setStage(533);
            users.get(users.get(id).getOpponent()).setStage(53);
            String txt = "Ваш противник ходит первым, ожидайте...";
            execute(new SendMessage(id, txt));
            String txt2 = "Вы ходите первым. Выбирайте";
            execute(new SendMessage(users.get(id).getOpponent(), txt2)
                    .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt2)));
        }
    }
    private void stepOne(Long id, String text) throws TelegramApiException {
        User user = users.get(id);
        if (text.contains("амен"))
            user.setElement(1);
        if (text.contains("ожниц"))
            user.setElement(2);
        if (text.contains("умаг"))
            user.setElement(3);
        String txt = "Вы сделали свой ход. Теперь ход противника. Ожидайте...";
        Long opp = user.getOpponent();
        execute(new SendMessage(id, txt)
                .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
        user.setStage(534);
        txt = "Ваш соперник сделал свой ход, теперь ваш ход.";
        execute(new SendMessage(opp, txt)
                .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard(txt)));
        users.get(opp).setStage(53);
    }

    private void stepTwo(Long id, String text) throws TelegramApiException {
        User user = users.get(id);
        if (text.contains("амен")) {
            stoneCommandTwo(id, 1);
        }
        if (text.contains("ожниц")) {
            stoneCommandTwo(id, 2);
        }
        if (text.contains("умаг")) {
            stoneCommandTwo(id, 3);
        }
    }

    public void stoneCommandTwo(Long id, int myElement) throws TelegramApiException {
        //Камень - 1, ножницы - 2, бумага - 3;
        User user = users.get(id);
        String[] elements = {" ","Камень","Ножницы","Бумага"};
        User opponent = users.get(user.getOpponent());

            if (opponent.getElement() == myElement) {
                execute(new SendMessage(id, "Ничья! Вы оба выбрали " + elements[myElement] + " !!!"));
                execute(new SendMessage(opponent.getID(), "Ничья! Вы оба выбрали " + elements[myElement] + " !!!"));
                //user.plusCASH(user.getBet());
                //execute(new SendMessage(id, "На ваш счет вернулась ваша ставка.\n"+"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
                execute(new SendMessage(opponent.getID(), "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((opponent.getElement() == 1 && myElement == 3) || (opponent.getElement() == 2 && myElement == 1)
                    || (opponent.getElement() == 3 && myElement == 2) ){
                execute(new SendMessage(id, "Вы выиграли!!!\nВаш противник выбрал "+ elements[opponent.getElement()] +" !"));
                execute(new SendMessage(opponent.getID(), "Вы проиграли((\nВаш противник выбрал "+ elements[myElement] +" !"));
                //user.plusCASH((user.getBet() * 2));
                //execute(new SendMessage(id,"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
                execute(new SendMessage(opponent.getID(), "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((myElement == 2 && opponent.getElement() == 1)|| (myElement == 3 && opponent.getElement() == 2)
                    || (myElement == 1 && opponent.getElement() == 3)){
                execute(new SendMessage(id, "Вы проиграли :( \nВаш противник выбрал "+elements[opponent.getElement()]+ "!"));
                execute(new SendMessage(opponent.getID(), "Вы выиграли!!!\nВаш противник выбрал "+elements[myElement]+ "!"));
                //execute(new SendMessage(id,"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
                execute(new SendMessage(opponent.getID(), "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
        user.setStage(0);
        opponent.setStage(0);
    }

    /**
     * Старт игры против бота
     */
    public void startCommand(Long id){
        int element;
        Random random = new Random();
        try {
            execute(new SendMessage(id, "Начнем игру! \nБот готовится..."));
            Thread.sleep(random.nextInt(2000) + 2000);
            if(random.nextBoolean()){
                execute(new SendMessage(id, "Вы ходите первым.")
                        .setReplyMarkup(keyboard.keyboard).setText(keyboard.getKeyboard("Вы ходите первым.")));
                element = random.nextInt(2) + 1;
            }else {
                execute(new SendMessage(id, "Бот ходит первым, ожидайте..."));
                Thread.sleep(random.nextInt(2000) + 2000);
                element = random.nextInt(2) + 1;
                execute(new SendMessage(id, "").setReplyMarkup(keyboard.keyboard)
                        .setText(keyboard.getKeyboard("Бот сделал свой ход." +
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
        User user = users.get(id);
        String[] elements = {" ","Камень","Ножницы","Бумага"};
        int element = users.get(id).getElement();
        try {
            if (elements[element].equals(elements[myElement])) {
                execute(new SendMessage(id, "Ничья! Вы оба выбрали " + elements[myElement] + " !!!"));
                user.plusCASH(user.getBet());
                //execute(new SendMessage(id, "На ваш счет вернулась ваша ставка.\n"+"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((myElement == 1 && element == 2)|| (myElement == 2 && element == 3)
                    || (myElement == 3 && element ==1)){
                execute(new SendMessage(id, "Вы выиграли!!!\nВаш противник выбрал "+ elements[element] +" !"));
                user.plusCASH((user.getBet() * 2));
                //execute(new SendMessage(id,"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
            if ((myElement == 2 && element == 1)|| (myElement == 3 && element == 2)
                    || (myElement == 1 && element == 3)){
                execute(new SendMessage(id, "Вы проиграли :( \nВаш противник выбрал "+elements[element]+ "!"));
                //execute(new SendMessage(id,"Ваш баланс: " + user.getCASH() + " рублей"));
                execute(new SendMessage(id, "Начать новую игру?").setText(keyboard.getKeyboard("Начать новую игру?")));
            }
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
        users.get(chatId).setStage(0);
    }

    public Long checkMap(String txt){
        for (User user : users.values()) {
            if (user.checkName(txt)){
                return user.getID();
            }
        }
        return null;
    }

    private void checkUsers() throws TelegramApiException {
        if (users.size() >=2) {
            List<String> usersList = new ArrayList<>();
            for (Map.Entry<Long, User> entry : users.entrySet()) {
                usersList.add(entry.getValue().getName());
            }
            String txt = "Сейчас в боте онлайн " + users.size() +
                    "\n" + usersList + "\nВведите имя с кем хотите сыграть";
            execute(new SendMessage(chatId, txt)
                    .setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash(txt)));
            users.get(chatId).setStage(50);
        }else {
            String txt = "Игроков онлайн не достаточно. Вы можете начать игру с ботом по команде /Новая игра";
            execute(new SendMessage(chatId, txt)
                    .setReplyMarkup(keyboard.keyboard)
                    .setText(keyboard.getKeyboardCash(txt)));
            users.get(chatId).setStage(0);
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

    private void cashMinus(Long chatId, Integer sum) throws TelegramApiException{
        User user = users.get(chatId);
        if (user.minusCASH(sum)) {
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
}
